package gub.rionegro.syslr.data;


import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "clients")
public class Client extends AbstractEntity {

    @Column(nullable = false, unique = true)
    private String telefono;  // "+598XXXXXXXX"

    private String nombre;

    @Column(unique = true)
    private String documento;  // CI, RUT, etc.

    private String email;

    @Enumerated(EnumType.STRING)
    private ChannelType channel;  // WHATSAPP, TELEGRAM, WEB

    @Column(length = 2000)
    private String metadata;  // JSON con preferencias, datos adicionales

    private Instant fechaRegistro;
    private Instant ultimoAcceso;
    private boolean consentimientoDatos;

    // Getters y Setters
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDocumento() { return documento; }
    public void setDocumento(String documento) { this.documento = documento; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public ChannelType getChannel() { return channel; }
    public void setChannel(ChannelType channel) { this.channel = channel; }

    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }

    public Instant getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(Instant fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public Instant getUltimoAcceso() { return ultimoAcceso; }
    public void setUltimoAcceso(Instant ultimoAcceso) { this.ultimoAcceso = ultimoAcceso; }

    public boolean isConsentimientoDatos() { return consentimientoDatos; }
    public void setConsentimientoDatos(boolean consentimientoDatos) {
        this.consentimientoDatos = consentimientoDatos;
    }
}