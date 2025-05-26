package gub.rionegro.syslr.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import gub.rionegro.syslr.data.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class ClientService {

    private final ClientRepository clientRepo;
    private final ObjectMapper objectMapper;

    public ClientService(ClientRepository clientRepo, ObjectMapper objectMapper) {
        this.clientRepo = clientRepo;
        this.objectMapper = objectMapper;
    }

    /**
     * Registrar un nuevo cliente o recuperar existente
     */
    @Transactional
    public Client registerOrGetClient(String telefono, ChannelType channel, String nombre) {
        return clientRepo.findByTelefono(telefono)
                .map(existingClient -> {
                    // Actualizar datos si es necesario
                    if (nombre != null && !nombre.isBlank() &&
                            (existingClient.getNombre() == null || existingClient.getNombre().isBlank())) {
                        existingClient.setNombre(nombre);
                    }
                    existingClient.setUltimoAcceso(Instant.now());
                    existingClient.setChannel(channel); // Actualizar canal si cambió
                    return clientRepo.save(existingClient);
                })
                .orElseGet(() -> {
                    // Crear nuevo cliente
                    Client newClient = new Client();
                    newClient.setTelefono(telefono);
                    newClient.setChannel(channel);
                    newClient.setNombre(nombre);
                    newClient.setFechaRegistro(Instant.now());
                    newClient.setUltimoAcceso(Instant.now());
                    newClient.setConsentimientoDatos(true); // Asumimos consentimiento por usar el servicio
                    return clientRepo.save(newClient);
                });
    }

    /**
     * Actualizar información del cliente
     */
    @Transactional
    public Client updateClientInfo(String telefono, String field, String value) {
        Client client = clientRepo.findByTelefono(telefono)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado: " + telefono));

        switch (field.toUpperCase()) {
            case "NOMBRE":
                client.setNombre(value);
                break;
            case "DOCUMENTO":
                client.setDocumento(value);
                break;
            case "EMAIL":
                client.setEmail(value);
                break;
            case "METADATA":
                client.setMetadata(value);
                break;
            case "CONSENTIMIENTO":
                client.setConsentimientoDatos(Boolean.parseBoolean(value));
                break;
            default:
                throw new IllegalArgumentException("Campo no válido: " + field);
        }

        client.setUltimoAcceso(Instant.now());
        return clientRepo.save(client);
    }

    /**
     * Buscar cliente por teléfono
     */
    public Optional<Client> findByTelefono(String telefono) {
        return clientRepo.findByTelefono(telefono);
    }

    /**
     * Buscar cliente por documento
     */
    public Optional<Client> findByDocumento(String documento) {
        return clientRepo.findByDocumento(documento);
    }

    /**
     * Buscar clientes por nombre (búsqueda parcial)
     */
    public List<Client> findByName(String nombre) {
        return clientRepo.findByNombreContainingIgnoreCase(nombre);
    }

    /**
     * Agregar o actualizar metadata del cliente
     */
    @Transactional
    public Client updateClientMetadata(String telefono, String key, String value) {
        Client client = clientRepo.findByTelefono(telefono)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado: " + telefono));

        try {
            JsonNode metadata;
            if (client.getMetadata() == null || client.getMetadata().isBlank()) {
                metadata = objectMapper.createObjectNode();
            } else {
                metadata = objectMapper.readTree(client.getMetadata());
            }

            if (metadata.isObject()) {
                ((ObjectNode) metadata).put(key, value);
                client.setMetadata(objectMapper.writeValueAsString(metadata));
                client.setUltimoAcceso(Instant.now());
                return clientRepo.save(client);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error actualizando metadata", e);
        }

        return client;
    }

    /**
     * Obtener metadata específica del cliente
     */
    public Optional<String> getClientMetadata(String telefono, String key) {
        return clientRepo.findByTelefono(telefono)
                .flatMap(client -> {
                    try {
                        if (client.getMetadata() != null && !client.getMetadata().isBlank()) {
                            JsonNode metadata = objectMapper.readTree(client.getMetadata());
                            if (metadata.has(key)) {
                                return Optional.of(metadata.get(key).asText());
                            }
                        }
                    } catch (Exception e) {
                        // Log error but don't fail
                    }
                    return Optional.empty();
                });
    }

    /**
     * Obtener estadísticas del cliente
     */
    public ClientStats getClientStats(String telefono) {
        Client client = clientRepo.findByTelefono(telefono)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        return new ClientStats(
                client.getNombre(),
                client.getTelefono(),
                client.getChannel().getDisplayName(),
                client.getFechaRegistro(),
                client.getUltimoAcceso(),
                isClientActive(client)
        );
    }

    /**
     * Verificar si un cliente está activo (accedió en los últimos 30 días)
     */
    public boolean isClientActive(Client client) {
        if (client.getUltimoAcceso() == null) return false;
        return client.getUltimoAcceso()
                .isAfter(Instant.now().minus(30, ChronoUnit.DAYS));
    }

    /**
     * Buscar clientes inactivos
     */
    public List<Client> findInactiveClients(int daysThreshold) {
        Instant threshold = Instant.now().minus(daysThreshold, ChronoUnit.DAYS);
        return clientRepo.findInactiveClients(threshold);
    }

    /**
     * Clase DTO para estadísticas del cliente
     */
    public record ClientStats(
            String nombre,
            String telefono,
            String canal,
            Instant fechaRegistro,
            Instant ultimoAcceso,
            boolean activo
    ) {}
}