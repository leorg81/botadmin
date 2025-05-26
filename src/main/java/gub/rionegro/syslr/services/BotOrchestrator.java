package gub.rionegro.syslr.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import gub.rionegro.syslr.data.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class BotOrchestrator {

    private final MenuService menuService;
    private final ClientService clientService;
    private final BotSessionRepo sessionRepo;
    private final BotFlowRepo flowRepo;
    private final BotStepRepo stepRepo;
    private final BotTransitionRepo transitionRepo;
    private final ClientRepository clientRepository;
    private final BotMenuRepository menuRepository;
    private final ObjectMapper mapper = new ObjectMapper();

    public BotOrchestrator(MenuService menuService,
                           ClientService clientService,
                           BotSessionRepo sessionRepo,
                           BotFlowRepo flowRepo,
                           BotStepRepo stepRepo,
                           BotTransitionRepo transitionRepo,
                           ClientRepository clientRepository,
                           BotMenuRepository menuRepository) {
        this.menuService = menuService;
        this.clientService = clientService;
        this.sessionRepo = sessionRepo;
        this.flowRepo = flowRepo;
        this.stepRepo = stepRepo;
        this.transitionRepo = transitionRepo;
        this.clientRepository = clientRepository;
        this.menuRepository = menuRepository;
    }

    public String getPromptFor(String telefono) {
        BotSession session = sessionRepo.findByTelefono(telefono)
                .orElseGet(() -> bootstrapSession(telefono));

        if (session.getFlow() == null) {
            return menuService.buildMenuMessage(session.getMenu().getComando());
        }

        return session.getStep().getMessage();
    }

    public NextMessage handleAnswer(String telefono, String respuesta) {
        BotSession session = sessionRepo.findByTelefono(telefono)
                .orElseThrow(() -> new RuntimeException("No hay sesión para el teléfono: " + telefono));

        // Si no hay flujo activo, procesar selección de menú
        if (session.getFlow() == null) {
            return procesarSeleccionMenu(session, respuesta);
        }

        // Procesar paso del flujo activo
        return procesarPasoFlujo(session, respuesta);
    }

    private NextMessage procesarSeleccionMenu(BotSession session, String respuesta) {
        try {
            int opcion = Integer.parseInt(respuesta);
            BotFlow flujoSeleccionado = menuService.getFlowByMenuSelection(
                    session.getMenu().getComando(), opcion);

            // Buscar primer paso del flujo
            BotStep primerPaso = stepRepo.findByFlowAndCode(flujoSeleccionado, "INICIO")
                    .orElseGet(() -> stepRepo.findByFlowOrderByOrderIndexAsc(flujoSeleccionado)
                            .stream()
                            .findFirst()
                            .orElseThrow(() -> new RuntimeException("Flujo sin pasos")));

            session.setFlow(flujoSeleccionado);
            session.setStep(primerPaso);
            session.setStatus(SessionStatus.ACTIVE);
            session.setUpdatedAt(Instant.now());
            sessionRepo.save(session);

            return NextMessage.ok(primerPaso.getMessage());
        } catch (NumberFormatException e) {
            return NextMessage.error("Debe ingresar un número. Ejemplo: 1, 2, 3...\n\n" +
                    menuService.buildMenuMessage(session.getMenu().getComando()));
        } catch (Exception e) {
            return NextMessage.error("Error: " + e.getMessage() + "\n\n" +
                    menuService.buildMenuMessage(session.getMenu().getComando()));
        }
    }

    private NextMessage procesarPasoFlujo(BotSession session, String respuesta) {
        BotStep paso = session.getStep();

        if (paso == null) {
            // Error: paso nulo, volver al menú
            return volverAlMenu(session, "Hubo un error en el flujo. Volviendo al menú principal.");
        }

        // Validar respuesta
        String error = validarRespuesta(paso, respuesta);
        if (error != null) {
            return NextMessage.error(error + "\n\n" + paso.getMessage());
        }

        // Guardar respuesta en datos de sesión
        ObjectNode datos = (ObjectNode) leerJson(session.getDataJson());
        datos.put(paso.getCode(), respuesta);
        session.setDataJson(escribirJson(datos));

        // Si es paso terminal, ejecutar acción
        if (Boolean.TRUE.equals(paso.isTerminal())) {
            String resultado = ejecutarAccion(session.getFlow(), datos);
            actualizarClienteDesdeSesion(session.getClient(), session);
            return volverAlMenu(session, resultado);
        }

        // Buscar siguiente paso
        BotStep siguientePaso = obtenerSiguientePaso(paso, respuesta);

        if (siguientePaso == null) {
            // No hay más pasos, terminar flujo
            String resultado = "✅ Proceso completado exitosamente.";
            actualizarClienteDesdeSesion(session.getClient(), session);
            return volverAlMenu(session, resultado);
        }

        // Avanzar al siguiente paso
        session.setStep(siguientePaso);
        session.setUpdatedAt(Instant.now());
        sessionRepo.save(session);

        return NextMessage.ok(siguientePaso.getMessage());
    }

    private NextMessage volverAlMenu(BotSession session, String mensajeFinal) {
        BotMenu menuPrincipal = menuRepository.findByComando("MAIN")
                .orElseGet(() -> crearMenuPrincipal());

        session.setFlow(null);
        session.setStep(null);
        session.setMenu(menuPrincipal);
        session.setStatus(SessionStatus.MENU);
        session.setDataJson("{}");
        session.setUpdatedAt(Instant.now());
        sessionRepo.save(session);

        String mensajeMenu = menuService.buildMenuMessage(menuPrincipal.getComando());
        return NextMessage.ok(mensajeFinal + "\n\n" + mensajeMenu);
    }

    private BotStep obtenerSiguientePaso(BotStep pasoActual, String respuesta) {
        List<BotTransition> transiciones = transitionRepo.findByFromStep(pasoActual);

        if (transiciones.isEmpty()) {
            return null;
        }

        // 1. Buscar transición con condición específica
        for (BotTransition transicion : transiciones) {
            if (transicion.getConditionKey() != null &&
                    !transicion.getConditionKey().isBlank() &&
                    respuesta.matches(transicion.getConditionKey())) {
                return transicion.getToStep();
            }
        }

        // 2. Buscar transición por defecto (.* o null)
        for (BotTransition transicion : transiciones) {
            if (transicion.getConditionKey() == null ||
                    ".*".equals(transicion.getConditionKey())) {
                return transicion.getToStep();
            }
        }

        return null;
    }

    private String validarRespuesta(BotStep paso, String entrada) {
        String regex = paso.getRegexOverride();

        if (regex == null || regex.isBlank()) {
            regex = regexPorDefecto(paso.getAnswerType());
        }

        if (regex != null && !entrada.matches(regex)) {
            return mensajeErrorValidacion(paso.getAnswerType());
        }

        return null;
    }

    private String regexPorDefecto(AnswerType tipo) {
        return switch (tipo) {
            case OPCION_MENU -> "^[1-9]\\d*$";
            case NUMERO -> "^\\d+$";
            case FECHA_MM_YYYY -> "^(0[1-9]|1[0-2])\\/\\d{4}$";
            case DOCUMENTO -> "^\\d{6,12}$";
            case CEDULA -> "^\\d{7,8}$";
            case SERIE_CIVICA -> "^[A-Z]{3}\\d{5}$";
            case TEXTO -> "^[\\p{L}0-9 .,_-]{1,50}$";
        };
    }

    private String mensajeErrorValidacion(AnswerType tipo) {
        return switch (tipo) {
            case FECHA_MM_YYYY -> "Formato inválido. Use MM/AAAA.";
            case OPCION_MENU -> "Opción inválida. Ingrese el número del menú.";
            case NUMERO -> "Ingrese solo números.";
            case DOCUMENTO -> "Documento inválido.";
            case CEDULA -> "Cédula inválida.";
            case SERIE_CIVICA -> "Serie cívica inválida (ej: AB123456).";
            case TEXTO -> "Texto inválido (máximo 50 caracteres).";
        };
    }

    private JsonNode leerJson(String json) {
        try {
            return json == null ? mapper.createObjectNode() : mapper.readTree(json);
        } catch (Exception e) {
            return mapper.createObjectNode();
        }
    }

    private String escribirJson(JsonNode nodo) {
        try {
            return mapper.writeValueAsString(nodo);
        } catch (Exception e) {
            return "{}";
        }
    }

    private BotSession bootstrapSession(String telefono) {
        // Registrar/obtener cliente (WhatsApp por defecto)
        Client cliente = clientService.registerOrGetClient(telefono, ChannelType.WHATSAPP, null);

        // Obtener o crear menú principal
        BotMenu menuPrincipal = menuRepository.findByComando("MAIN")
                .orElseGet(() -> crearMenuPrincipal());

        // Crear nueva sesión
        BotSession sesion = new BotSession();
        sesion.setClient(cliente);
        sesion.setMenu(menuPrincipal);
        sesion.setStatus(SessionStatus.MENU);
        sesion.setDataJson("{}");
        sesion.setStartedAt(Instant.now());
        sesion.setUpdatedAt(Instant.now());
        sesion.setExpiresAt(Instant.now().plus(2, ChronoUnit.HOURS));

        return sessionRepo.save(sesion);
    }

    private BotMenu crearMenuPrincipal() {
        BotMenu menu = new BotMenu(
                "Menú Principal",
                "MAIN",
                "Bienvenido! Seleccione una opción:"
        );
        return menuRepository.save(menu);
    }

    private void actualizarClienteDesdeSesion(Client cliente, BotSession sesion) {
        ObjectNode datos = (ObjectNode) leerJson(sesion.getDataJson());

        if (datos.has("DOCUMENTO")) {
            cliente.setDocumento(datos.get("DOCUMENTO").asText());
        }
        if (datos.has("NOMBRE")) {
            cliente.setNombre(datos.get("NOMBRE").asText());
        }
        if (datos.has("EMAIL")) {
            cliente.setEmail(datos.get("EMAIL").asText());
        }

        cliente.setUltimoAcceso(Instant.now());
        clientRepository.save(cliente);
    }

    private String ejecutarAccion(BotFlow flujo, ObjectNode datos) {
        String codigoFlujo = flujo.getCode();

        // Aquí implementarías la lógica específica para cada flujo
        switch (codigoFlujo) {
            case "RECIBOS":
                return procesarRecibo(datos);
            case "TURNOS":
                return procesarTurno(datos);
            case "DEUDAS":
                return procesarDeuda(datos);
            default:
                return "✅ Solicitud procesada correctamente. ID: " + Instant.now().toEpochMilli();
        }
    }

    private String procesarRecibo(ObjectNode datos) {
        // Ejemplo de procesamiento
        String documento = datos.has("DOCUMENTO") ? datos.get("DOCUMENTO").asText() : "No especificado";
        String tipo = datos.has("TIPO") ? datos.get("TIPO").asText() : "No especificado";

        // TODO: Llamar al servicio real de recibos
        // reciboService.generarRecibo(documento, tipo);

        return String.format(
                "✅ *Recibo solicitado*\n\n" +
                        "Documento: %s\n" +
                        "Tipo: %s\n\n" +
                        "Se enviará a su correo registrado.",
                documento, tipo
        );
    }

    private String procesarTurno(ObjectNode datos) {
        // TODO: Implementar lógica de turnos
        return "✅ Turno reservado exitosamente.";
    }

    private String procesarDeuda(ObjectNode datos) {
        // TODO: Implementar lógica de deudas
        return "✅ Consulta realizada. No hay deudas pendientes.";
    }

    // Record para respuesta
    public record NextMessage(boolean ok, String mensaje) {
        public static NextMessage ok(String mensaje) {
            return new NextMessage(true, mensaje);
        }
        public static NextMessage error(String mensaje) {
            return new NextMessage(false, mensaje);
        }
    }

    // Métodos auxiliares
    private boolean isPersonalDataStep(BotStep paso) {
        if (paso == null) return false;
        String codigo = paso.getCode().toUpperCase();
        return codigo.contains("NOMBRE") ||
                codigo.contains("DOCUMENTO") ||
                codigo.contains("EMAIL") ||
                codigo.contains("TELEFONO") ||
                codigo.contains("CEDULA");
    }

    private void updateClientFromAnswer(Client cliente, BotStep paso, String respuesta) {
        String codigo = paso.getCode().toUpperCase();

        if (codigo.contains("NOMBRE")) {
            clientService.updateClientInfo(cliente.getTelefono(), "NOMBRE", respuesta);
        } else if (codigo.contains("DOCUMENTO") || codigo.contains("CEDULA")) {
            clientService.updateClientInfo(cliente.getTelefono(), "DOCUMENTO", respuesta);
        } else if (codigo.contains("EMAIL")) {
            clientService.updateClientInfo(cliente.getTelefono(), "EMAIL", respuesta);
        }
    }

    // Métodos públicos adicionales
    public Optional<BotSession> obtenerSesionActiva(String telefono) {
        return sessionRepo.findByTelefono(telefono)
                .filter(s -> s.getStatus() == SessionStatus.ACTIVE ||
                        s.getStatus() == SessionStatus.MENU);
    }

    public void cerrarSesion(String telefono) {
        sessionRepo.findByTelefono(telefono).ifPresent(sesion -> {
            sesion.setStatus(SessionStatus.COMPLETED);
            sesion.setCompletedAt(Instant.now());
            sesion.setUpdatedAt(Instant.now());
            sessionRepo.save(sesion);
        });
    }

    public ClientService.ClientStats obtenerEstadisticasCliente(String telefono) {
        return clientService.getClientStats(telefono);
    }
}