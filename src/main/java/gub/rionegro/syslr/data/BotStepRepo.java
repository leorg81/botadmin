package gub.rionegro.syslr.data;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BotStepRepo extends JpaRepository<BotStep, Long> {
    Optional<BotStep> findByFlowAndCode(BotFlow flow, String code);
    List<BotStep> findByFlowOrderByOrderIndexAsc(BotFlow flow);
}