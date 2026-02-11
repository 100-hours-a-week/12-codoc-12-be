package _ganzi.codoc.analysis.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@RequiredArgsConstructor
@EnableConfigurationProperties(AnalysisServerProperties.class)
@Configuration
public class AnalysisServerConfig {

    private final AnalysisServerProperties analysisServerProperties;

    @Bean(name = "analysisServerWebClientBuilder")
    public WebClient.Builder analysisServerWebClientBuilder() {
        return WebClient.builder().baseUrl(analysisServerProperties.baseUrl());
    }
}
