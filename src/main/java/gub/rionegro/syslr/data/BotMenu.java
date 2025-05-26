package gub.rionegro.syslr.data;

import jakarta.persistence.Entity;

@Entity
public class BotMenu extends AbstractEntity{
    private String titulo;
    private String comando;
    private String descripcion;

    public BotMenu() {}
    public BotMenu( String titulo, String comando, String descripcion) {
        this.titulo = titulo;
        this.comando = comando;
        this.descripcion = descripcion;
    }

    // getters y setters

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getComando() { return comando; }
    public void setComando(String comando) { this.comando = comando; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}
