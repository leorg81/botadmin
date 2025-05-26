package gub.rionegro.syslr.data;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BotMenuRepository extends JpaRepository<BotMenu,Long> {
         Optional<BotMenu> findById(Long id);
         Optional<BotMenu> findByComando(String comando);

}
