package gub.rionegro.syslr.controllers;


import gub.rionegro.syslr.data.Client;
import gub.rionegro.syslr.services.ClientService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/clients")
@CrossOrigin(origins = "*")
public class ClientController {

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @GetMapping("/{telefono}")
    public ResponseEntity<Client> getClient(@PathVariable String telefono) {
        return clientService.findByTelefono(telefono)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public ResponseEntity<List<Client>> searchClients(
            @RequestParam(required = false) String telefono,
            @RequestParam(required = false) String documento,
            @RequestParam(required = false) String nombre) {

        if (telefono != null) {
            return ResponseEntity.ok(clientService.findByTelefono(telefono)
                    .map(List::of)
                    .orElse(List.of()));
        }

        if (documento != null) {
            return ResponseEntity.ok(clientService.findByDocumento(documento)
                    .map(List::of)
                    .orElse(List.of()));
        }

        if (nombre != null) {
            return ResponseEntity.ok(clientService.findByName(nombre));
        }

        return ResponseEntity.badRequest().build();
    }

    @PutMapping("/{telefono}/metadata")
    public ResponseEntity<Client> updateMetadata(
            @PathVariable String telefono,
            @RequestBody Map<String, String> metadata) {

        try {
            Client updatedClient = null;
            for (Map.Entry<String, String> entry : metadata.entrySet()) {
                updatedClient = clientService.updateClientMetadata(
                        telefono, entry.getKey(), entry.getValue());
            }
            return ResponseEntity.ok(updatedClient);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{telefono}/stats")
    public ResponseEntity<ClientService.ClientStats> getStats(@PathVariable String telefono) {
        try {
            return ResponseEntity.ok(clientService.getClientStats(telefono));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}