package _ganzi.codoc.problem.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.recommend.mq")
public record RecommendMqProperties(
        boolean enabled,
        boolean requestPublishEnabled,
        boolean responseConsumeEnabled,
        String exchange,
        String requestQueue,
        String requestDlq,
        String responseQueue,
        String responseDlq,
        String requestRoutingKey,
        String requestDlqRoutingKey,
        String responseRoutingKey,
        String responseDlqRoutingKey) {}
