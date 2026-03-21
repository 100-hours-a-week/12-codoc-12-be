package _ganzi.codoc.custom.event;

import _ganzi.codoc.custom.service.CustomProblemGenerationDispatcher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@RequiredArgsConstructor
@Component
public class CustomProblemGenerationRequestedEventListener {

    private final CustomProblemGenerationDispatcher dispatcher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(CustomProblemGenerationRequestedEvent event) {
        dispatcher.dispatch(event.customProblemId());
    }
}
