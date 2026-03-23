package _ganzi.codoc.problem.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.recommend.outbox")
public record RecommendOutboxProperties(
        int batchSize,
        int maxBatchesPerRun,
        Duration publishFixedDelay,
        Duration processingTimeout,
        Duration retryInitialInterval,
        double retryMultiplier,
        Duration retryMaxInterval,
        int maxPublishAttempts) {}
