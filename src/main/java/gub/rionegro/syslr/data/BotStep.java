package gub.rionegro.syslr.data;

import jakarta.persistence.*;

@Entity
public class BotStep extends AbstractEntity {
    @ManyToOne(optional=false) private BotFlow flow;
    private String code;             // "MENU_PRINCIPAL", "PEDIR_FECHA", etc.
    @Column(length=1000) private String message; // "Ingrese la fecha MM/AAAA"
    @Enumerated(EnumType.STRING) private AnswerType answerType;
    private String regexOverride;    // opcional, si querés custom
    private boolean terminal;        // si es paso final (ejecuta acción)
    private Integer orderIndex;      // para listados/UX

    public BotFlow getFlow() {
        return flow;
    }

    public void setFlow(BotFlow flow) {
        this.flow = flow;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public AnswerType getAnswerType() {
        return answerType;
    }

    public void setAnswerType(AnswerType answerType) {
        this.answerType = answerType;
    }

    public String getRegexOverride() {
        return regexOverride;
    }

    public void setRegexOverride(String regexOverride) {
        this.regexOverride = regexOverride;
    }

    public boolean isTerminal() {
        return terminal;
    }

    public void setTerminal(boolean terminal) {
        this.terminal = terminal;
    }

    public Integer getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(Integer orderIndex) {
        this.orderIndex = orderIndex;
    }
}