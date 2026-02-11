package _ganzi.codoc.ai.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.reactive.function.client.WebClient;

@RequiredArgsConstructor
@EnableConfigurationProperties(AiServerProperties.class)
@Configuration
public class AiServerConfig {

    private final AiServerProperties aiServerProperties;

    @Bean(name = "aiServerWebClientBuilder")
    @Primary
    public WebClient.Builder aiServerWebClientBuilder() {
        return WebClient.builder().baseUrl(aiServerProperties.baseUrl());
    }
}
