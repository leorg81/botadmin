package gub.rionegro.syslr.data;

import jakarta.persistence.*;

import java.time.Instant;
@Entity
public class BotSession extends AbstractEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "client_id")
    private Client client;

    @ManyToOne
    private BotFlow flow;

    @ManyToOne
    private BotStep step;

    @ManyToOne
    private BotMenu menu;  // Menú actual

    @Column(length = 4000)
    private String dataJson;

    @Enumerated(EnumType.STRING)
    private SessionStatus status;  // ACTIVE, COMPLETED, ABANDONED, ERROR

    private Instant startedAt;
    private Instant updatedAt;
    private Instant completedAt;
    private Instant expiresAt;  // Para timeout

    // Getters y Setters
    public Client getClient() { return client; }
    public void setClient(Client client) { this.client = client; }


    public BotMenu getMenu() { return menu; }
    public void setMenu(BotMenu menu) { this.menu = menu; }

    public SessionStatus getStatus() { return status; }
    public void setStatus(SessionStatus status) { this.status = status; }

    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }

    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }

    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }

    public BotFlow getFlow() {

        return flow;
    }

    public void setFlow(BotFlow flow) {
        this.flow = flow;
    }

    public BotStep getStep() {
        return step;
    }

    public void setStep(BotStep step) {
        this.step = step;
    }

    public String getDataJson() {
        return dataJson;
    }

    public void setDataJson(String dataJson) {
        this.dataJson = dataJson;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}