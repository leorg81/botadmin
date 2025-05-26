package gub.rionegro.syslr.data;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BotTransitionRepo extends JpaRepository<BotTransition, Long> {
    List<BotTransition> findByFromStep(BotStep fromStep);
}