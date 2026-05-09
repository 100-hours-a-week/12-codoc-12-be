package _ganzi.codoc.problem.event;

import _ganzi.codoc.problem.service.ProblemContentCacheInvalidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@RequiredArgsConstructor
@Component
public class ProblemContentChangedEventListener {

    private final ProblemContentCacheInvalidationService invalidationService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(ProblemContentChangedEvent event) {
        invalidationService.invalidateAndPublish(event.problemId());
    }
}
