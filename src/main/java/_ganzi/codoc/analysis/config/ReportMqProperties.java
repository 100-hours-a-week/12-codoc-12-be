package _ganzi.codoc.analysis.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.report.mq")
public record ReportMqProperties(
        boolean enabled,
        boolean requestPublishEnabled,
        boolean responseConsumeEnabled,
        String exchange,
        String requestQueue,
        String responseQueue,
        String requestRoutingKey,
        String responseRoutingKey) {}
