package gub.rionegro.syslr.data;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;

@Entity
public class BotFlow extends AbstractEntity {
    private String code;   // "RECIBOS", "TURNOS"
    private String name;   // "Solicitar recibo", "Reservar turno"
    @ManyToOne
    private BotMenu menu;


    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BotMenu getMenu() {
        return menu;
    }

    public void setMenu(BotMenu menu) {
        this.menu = menu;
    }
}