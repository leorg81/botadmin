package gub.rionegro.syslr;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.theme.Theme;
import gub.rionegro.syslr.data.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * The entry point of the Spring Boot application.
 *
 * Use the @PWA annotation make the application installable on phones, tablets
 * and some desktop browsers.
 *
 */
@PWA(name = "Bot Admin - Río Negro ", shortName = "BotRN1.0")
@SpringBootApplication
@Theme(value = "my-app")
public class Application implements AppShellConfigurator {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);

    }

    @Bean
    CommandLineRunner seed(BotFlowRepo flowRepo, BotStepRepo stepRepo, BotTransitionRepo trRepo) {
        return args -> {
            if (flowRepo.findByCode("RECIBOS").isPresent()) return;

            BotFlow flow = new BotFlow();
            flow.setCode("RECIBOS"); flow.setName("Solicitar recibo");
            flow = flowRepo.save(flow);

            BotStep menu = new BotStep();
            menu.setFlow(flow); menu.setCode("MENU_PRINCIPAL");
            menu.setMessage("Menú:\n1) Recibo de sueldo\n2) Aguinaldo\n3) Salario vacacional\nElija opción:");
            menu.setAnswerType(AnswerType.OPCION_MENU);
            menu.setOrderIndex(1);
            stepRepo.save(menu);

            BotStep pedirFecha = new BotStep();
            pedirFecha.setFlow(flow); pedirFecha.setCode("PEDIR_FECHA");
            pedirFecha.setMessage("Ingrese la fecha en formato MM/AAAA:");
            pedirFecha.setAnswerType(AnswerType.FECHA_MM_YYYY);
            pedirFecha.setOrderIndex(2);
            stepRepo.save(pedirFecha);

            BotStep pedirDoc = new BotStep();
            pedirDoc.setFlow(flow); pedirDoc.setCode("PEDIR_DOCUMENTO");
            pedirDoc.setMessage("Ingrese su número de documento:");
            pedirDoc.setAnswerType(AnswerType.DOCUMENTO);
            pedirDoc.setOrderIndex(3);
            stepRepo.save(pedirDoc);

            BotStep confirmar = new BotStep();
            confirmar.setFlow(flow); confirmar.setCode("CONFIRMAR_ENVIO");
            confirmar.setMessage("Procesando… ✅");
            confirmar.setAnswerType(AnswerType.TEXTO);
            confirmar.setTerminal(true);
            confirmar.setOrderIndex(4);
            stepRepo.save(confirmar);

            // Transiciones: 1|2|3 → pedirFecha → pedirDoc → confirmar
            trRepo.save(newTransition(menu, pedirFecha, "^[1-3]$"));
            trRepo.save(newTransition(pedirFecha, pedirDoc, ".*"));
            trRepo.save(newTransition(pedirDoc, confirmar, ".*"));
        };
    }

    private BotTransition newTransition(BotStep from, BotStep to, String key) {
        BotTransition t = new BotTransition();
        t.setFromStep(from); t.setToStep(to); t.setConditionKey(key);
        return t;
    }


}
