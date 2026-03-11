package _ganzi.codoc.notification.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.notification.mq")
public record NotificationMqProperties(
        boolean enabled,
        String exchange,
        String inAppQueue,
        String inAppDlq,
        String pushQueue,
        String pushDlq,
        int retryMaxAttempts,
        long retryInitialIntervalMillis,
        double retryMultiplier,
        long retryMaxIntervalMillis) {}
