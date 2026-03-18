package _ganzi.codoc.analysis.mq;

import _ganzi.codoc.analysis.config.ReportMqProperties;
import _ganzi.codoc.analysis.dto.AnalysisReportRequest;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "app.report.mq", name = "enabled", havingValue = "true")
public class ReportRequestPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final ReportMqProperties properties;

    public ReportRequestPublisher(
            @Qualifier("reportRabbitTemplate") RabbitTemplate rabbitTemplate,
            ReportMqProperties properties) {
        this.rabbitTemplate = rabbitTemplate;
        this.properties = properties;
    }

    public void publish(String jobId, AnalysisReportRequest payload, Instant requestedAt) {
        ReportRequestMessage message = new ReportRequestMessage(jobId, requestedAt, payload);
        rabbitTemplate.convertAndSend(
                properties.exchange(),
                properties.requestRoutingKey(),
                message,
                amqpMessage -> {
                    amqpMessage.getMessageProperties().setMessageId(jobId);
                    amqpMessage.getMessageProperties().setHeader("jobId", jobId);
                    return amqpMessage;
                },
                new CorrelationData(jobId));
        log.info("analysis report request published. jobId={}", jobId);
    }
}
