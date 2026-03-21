package _ganzi.codoc.custom.service;

import _ganzi.codoc.ai.dto.CustomProblemGenerationResponse;
import _ganzi.codoc.ai.dto.mq.CustomProblemMqResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;

@Slf4j
@RequiredArgsConstructor
public class CustomProblemGenerationResponseConsumer {

    private final CustomProblemGenerationResultService resultService;

    @RabbitListener(queues = "${app.custom-problem.mq.response-queue}")
    public void consume(CustomProblemMqResponse mqResponse) {
        log.info(
                "Received custom problem generation response. customProblemId={}",
                mqResponse.customProblemId());

        if (mqResponse.response().isFailure()) {
            log.warn(
                    "Custom problem generation failed. customProblemId={}, code={}",
                    mqResponse.customProblemId(),
                    mqResponse.response().code());
            resultService.fail(mqResponse.customProblemId());
            return;
        }

        CustomProblemGenerationResponse response = mqResponse.response().data();

        resultService.complete(mqResponse.customProblemId(), response);
        log.info(
                "Successfully completed custom problem generation. customProblemId={}",
                mqResponse.customProblemId());
    }
}
