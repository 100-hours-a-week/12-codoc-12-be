package _ganzi.codoc.problem.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.recommend.dlq")
public record RecommendDlqProperties(
        boolean alertEnabled,
        int alertBurstThreshold,
        Duration alertWindow,
        Duration monitorFixedDelay,
        int reprocessDefaultLimit,
        int reprocessMaxLimit,
        int reprocessSingleScanLimit) {}
