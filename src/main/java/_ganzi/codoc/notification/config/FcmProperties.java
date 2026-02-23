package _ganzi.codoc.notification.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.fcm")
public record FcmProperties(Boolean enabled, String projectId) {}
