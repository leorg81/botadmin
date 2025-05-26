package gub.rionegro.syslr.data;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface BotSessionRepo extends JpaRepository<BotSession, Long> {
    Optional<BotSession> findByTelefono(String telefono);
}