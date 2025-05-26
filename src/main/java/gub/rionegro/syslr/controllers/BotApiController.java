package gub.rionegro.syslr.controllers;

import gub.rionegro.syslr.services.BotOrchestrator;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/bot")
public class BotApiController {
    private final BotOrchestrator orchestrator;

    public BotApiController(BotOrchestrator orchestrator) { this.orchestrator = orchestrator; }

    // 1) Obtener mensaje a mostrar (primer contacto o prompt del paso actual)
    @GetMapping("/prompt")
    public Map<String,String> prompt(@RequestParam String tel) {
        String msg = orchestrator.getPromptFor(tel);
        return Map.of("mensaje", msg);
    }

    // 2) Enviar respuesta del usuario y recibir siguiente mensaje o error
    @PostMapping("/answer")
    public Map<String,Object> answer(@RequestParam String tel, @RequestBody Map<String,String> body) {
        String resp = body.getOrDefault("respuesta", "");
        var r = orchestrator.handleAnswer(tel, resp);
        return Map.of("ok", r.ok(), "mensaje", r.mensaje());
    }
}
