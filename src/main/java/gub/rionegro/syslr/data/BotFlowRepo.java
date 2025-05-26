package gub.rionegro.syslr.data;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BotFlowRepo extends JpaRepository<BotFlow, Long> {
    Optional<BotFlow> findByCode(String code);

    List<BotFlow> findByMenu(BotMenu menu);
}