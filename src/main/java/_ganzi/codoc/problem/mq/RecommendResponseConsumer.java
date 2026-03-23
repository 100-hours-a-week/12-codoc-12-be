package _ganzi.codoc.problem.mq;

import _ganzi.codoc.problem.service.RecommendResponseProcessingService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = "app.recommend.mq",
        name = {"enabled", "response-consume-enabled"},
        havingValue = "true")
public class RecommendResponseConsumer {

    private final RecommendResponseProcessingService recommendResponseProcessingService;

    @RabbitListener(
            queues = "${app.recommend.mq.response-queue}",
            containerFactory = "recommendRabbitListenerContainerFactory")
    public void consume(RecommendResponseMessage message) {
        recommendResponseProcessingService.process(message);
    }
}
