package gub.rionegro.syslr.data;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;

@Entity
public class BotTransition extends AbstractEntity {
    @ManyToOne(optional=false) private BotStep fromStep;
    @ManyToOne(optional=false) private BotStep toStep;
    private String conditionKey;     // p.ej. "1", "2", ".*" (default)

    public BotStep getFromStep() {
        return fromStep;
    }

    public void setFromStep(BotStep fromStep) {
        this.fromStep = fromStep;
    }

    public BotStep getToStep() {
        return toStep;
    }

    public void setToStep(BotStep toStep) {
        this.toStep = toStep;
    }

    public String getConditionKey() {
        return conditionKey;
    }

    public void setConditionKey(String conditionKey) {
        this.conditionKey = conditionKey;
    }
}