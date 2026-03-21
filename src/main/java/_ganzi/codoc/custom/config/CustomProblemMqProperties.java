package _ganzi.codoc.custom.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.custom-problem.mq")
public record CustomProblemMqProperties(
        boolean enabled,
        String exchange,
        String requestQueue,
        String responseQueue,
        String responseDlq) {}
