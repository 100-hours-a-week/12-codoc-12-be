package _ganzi.codoc.custom.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@ConditionalOnMissingBean(CustomProblemGenerationOrchestrator.class)
@Component
public class UnavailableCustomProblemGenerationDispatcher
        implements CustomProblemGenerationDispatcher {

    private final CustomProblemGenerationResultService resultService;

    @Async("customProblemTaskExecutor")
    @Override
    public void dispatch(Long customProblemId) {
        log.warn(
                "Custom problem generation is unavailable (MQ disabled). customProblemId={}",
                customProblemId);
        resultService.fail(customProblemId);
    }
}
