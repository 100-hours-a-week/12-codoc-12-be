package _ganzi.codoc.custom.service;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@ConditionalOnBean(CustomProblemGenerationOrchestrator.class)
@Component
public class AsyncCustomProblemGenerationDispatcher implements CustomProblemGenerationDispatcher {

    private final CustomProblemGenerationOrchestrator orchestrator;

    @Async("customProblemTaskExecutor")
    @Override
    public void dispatch(Long customProblemId) {
        orchestrator.process(customProblemId);
    }
}
