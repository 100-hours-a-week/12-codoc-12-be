package _ganzi.codoc.ai.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.ai-server")
public record AiServerProperties(String baseUrl, Duration baseTimeout, Duration recommendTimeout) {}
