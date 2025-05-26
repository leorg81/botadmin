package gub.rionegro.syslr.data;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;

@Entity
public class SessionData extends AbstractEntity {
    @ManyToOne
    private BotSession session;
    private String stepKey; // clave del paso actual
    private String response; // respuesta del usuario
    private String metadata; // JSON con datos adicionales
}
