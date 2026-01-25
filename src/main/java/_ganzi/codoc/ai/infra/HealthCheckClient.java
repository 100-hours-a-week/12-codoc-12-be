package _ganzi.codoc.ai.infra;

import _ganzi.codoc.ai.config.AiServerProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class HealthCheckClient {

    private static final String API_PATH_HEALTHCHECK = "/healthcheck";

    private final WebClient webClient;
    private final AiServerProperties aiServerProperties;

    public HealthCheckClient(WebClient.Builder builder, AiServerProperties aiServerProperties) {
        this.aiServerProperties = aiServerProperties;
        this.webClient = builder.build();
    }

    public String requestHealthCheck() {
        return webClient
                .get()
                .uri(API_PATH_HEALTHCHECK)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(aiServerProperties.baseTimeout())
                .block();
    }
}
