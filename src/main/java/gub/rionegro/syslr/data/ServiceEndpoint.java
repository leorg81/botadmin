package gub.rionegro.syslr.data;

import jakarta.persistence.Entity;

@Entity
public class ServiceEndpoint extends AbstractEntity {
    private String name; // "SolicitarRecibo"
    private String url;
    private String method; // POST, GET
    private String headers; // JSON
    private String payloadTemplate; // Plantilla con {variables}
}
