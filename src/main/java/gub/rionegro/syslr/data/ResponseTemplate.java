package gub.rionegro.syslr.data;

import jakarta.persistence.Entity;

@Entity
public class ResponseTemplate extends AbstractEntity {
    private String key; // "recibo_solicitado"
    private String message; // "Tu recibo ha sido generado: {numero}"
    private AnswerType answerType; // TEXT, MENU, etc.



}