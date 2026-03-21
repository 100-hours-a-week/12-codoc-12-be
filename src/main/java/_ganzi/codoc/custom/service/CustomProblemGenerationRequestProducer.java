package _ganzi.codoc.custom.service;

import _ganzi.codoc.ai.dto.CustomProblemGenerationRequest;
import _ganzi.codoc.ai.dto.mq.CustomProblemMqRequest;
import _ganzi.codoc.custom.config.CustomProblemMqProperties;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

@Slf4j
@RequiredArgsConstructor
public class CustomProblemGenerationRequestProducer {

    private final CustomProblemMqProperties mqProperties;
    private final RabbitTemplate rabbitTemplate;

    public void produce(Long customProblemId, List<CustomProblemGenerationRequest> images) {
        CustomProblemMqRequest mqRequest = new CustomProblemMqRequest(customProblemId, images);

        rabbitTemplate.convertAndSend(mqProperties.exchange(), mqProperties.requestQueue(), mqRequest);

        log.info("Produced custom problem generation request. customProblemId={}", customProblemId);
    }
}
