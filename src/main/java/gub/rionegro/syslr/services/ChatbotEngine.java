package gub.rionegro.syslr.services;


import gub.rionegro.syslr.data.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ChatbotEngine {

    @Autowired
    private BotSessionRepo sessionRepo;
    @Autowired
    private BotFlowRepo flowRepo;
    @Autowired
    private BotStepRepo stepRepo;
    @Autowired
    private BotTransitionRepo transitionRepo;
    @Autowired
    private ServiceEndpointService endpointService;

    public ProcessedResponse processMessage(String userId, String channel, String message) {
        // 1. Obtener o crear sesión
        BotSession session = sessionRepo.findByUserIdAndChannel(userId, channel)
                .orElseGet(() -> createNewSession(userId, channel));

        // 2. Determinar paso actual
        BotStep currentStep = getCurrentStep(session);

        // 3. Si no hay paso actual, mostrar menú principal
        if (currentStep == null) {
            return showMainMenu(session);
        }

        // 4. Validar respuesta según AnswerType
        ValidationResult validation = validateAnswer(currentStep, message);
        if (!validation.isValid()) {
            return new ProcessedResponse(validation.getError(), currentStep);
        }

        // 5. Guardar respuesta en SessionData
        saveResponse(session, currentStep, message);

        // 6. Buscar siguiente paso según transición
        BotTransition transition = transitionRepo
                .findByCurrentStepAndTriggerValue(currentStep, message);

        if (transition != null) {
            session.setCurrentStep(transition.getNextStep());
            sessionRepo.save(session);
        }

        // 7. Si el siguiente paso es null, finalizar flujo y llamar WS
        if (session.getCurrentStep() == null) {
            return finalizeFlow(session);
        }

        // 8. Devolver pregunta del siguiente paso
        return new ProcessedResponse(session.getCurrentStep().getQuestion(), session.getCurrentStep());
    }
}