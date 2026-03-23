package _ganzi.codoc.problem.service;

import _ganzi.codoc.global.alert.DiscordAlertService;
import _ganzi.codoc.problem.config.RecommendDlqProperties;
import _ganzi.codoc.problem.config.RecommendMqProperties;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.recommend.mq", name = "enabled", havingValue = "true")
public class RecommendDlqMonitorService {

    @Qualifier("recommendRabbitTemplate")
    private final RabbitTemplate recommendRabbitTemplate;

    private final RecommendMqProperties recommendMqProperties;
    private final RecommendDlqProperties recommendDlqProperties;
    private final DiscordAlertService discordAlertService;

    private final Deque<IngressEvent> ingressEvents = new ArrayDeque<>();
    private long previousDepth = 0L;
    private Instant lastBurstAlertAt = Instant.EPOCH;
    private Instant lastRecoveryAlertAt = Instant.EPOCH;

    @Scheduled(fixedDelayString = "${app.recommend.dlq.monitor-fixed-delay:1m}")
    public void monitor() {
        Long depth = currentDepth();
        if (depth == null) {
            return;
        }

        long delta = Math.max(0L, depth - previousDepth);
        if (delta > 0) {
            recordIngress(delta);
            notifyFirstIngressIfNeeded(depth);
        }

        notifyBurstIfNeeded(depth);
        notifyRecoveryIfNeeded(depth);

        previousDepth = depth;
    }

    private Long currentDepth() {
        try {
            return recommendRabbitTemplate.execute(
                    channel -> channel.messageCount(recommendMqProperties.responseDlq()));
        } catch (Exception exception) {
            log.warn(
                    "recommend dlq monitor failed to read queue depth. queue={}",
                    recommendMqProperties.responseDlq(),
                    exception);
            return null;
        }
    }

    private void notifyFirstIngressIfNeeded(long currentDepth) {
        if (!recommendDlqProperties.alertEnabled()) {
            return;
        }
        if (previousDepth == 0 && currentDepth > 0) {
            discordAlertService.send(
                    String.format(
                            "[recommend-dlq] first ingress detected. queue=%s depth=%d",
                            recommendMqProperties.responseDlq(), currentDepth));
        }
    }

    private void notifyBurstIfNeeded(long currentDepth) {
        if (!recommendDlqProperties.alertEnabled()) {
            return;
        }
        Instant now = Instant.now();
        cleanupOldIngress(now);
        long sum = ingressEvents.stream().mapToLong(IngressEvent::count).sum();
        if (sum < Math.max(1, recommendDlqProperties.alertBurstThreshold())) {
            return;
        }
        if (lastBurstAlertAt.plus(recommendDlqProperties.alertWindow()).isAfter(now)) {
            return;
        }
        lastBurstAlertAt = now;
        discordAlertService.send(
                String.format(
                        "[recommend-dlq] burst detected in %s. ingress=%d threshold=%d depth=%d queue=%s",
                        recommendDlqProperties.alertWindow(),
                        sum,
                        recommendDlqProperties.alertBurstThreshold(),
                        currentDepth,
                        recommendMqProperties.responseDlq()));
    }

    private void notifyRecoveryIfNeeded(long currentDepth) {
        if (!recommendDlqProperties.alertEnabled()) {
            return;
        }
        Instant now = Instant.now();
        if (previousDepth > 0 && currentDepth == 0) {
            if (lastRecoveryAlertAt.plus(recommendDlqProperties.alertWindow()).isAfter(now)) {
                return;
            }
            lastRecoveryAlertAt = now;
            discordAlertService.send(
                    String.format(
                            "[recommend-dlq] recovered. queue=%s depth=0", recommendMqProperties.responseDlq()));
        }
    }

    private void recordIngress(long delta) {
        ingressEvents.addLast(new IngressEvent(Instant.now(), delta));
    }

    private void cleanupOldIngress(Instant now) {
        Instant threshold = now.minus(recommendDlqProperties.alertWindow());
        while (!ingressEvents.isEmpty() && ingressEvents.peekFirst().timestamp().isBefore(threshold)) {
            ingressEvents.removeFirst();
        }
    }

    private record IngressEvent(Instant timestamp, long count) {}
}
