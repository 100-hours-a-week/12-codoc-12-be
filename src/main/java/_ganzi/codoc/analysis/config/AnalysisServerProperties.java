package _ganzi.codoc.analysis.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.analysis-server")
public record AnalysisServerProperties(
        String baseUrl, Duration baseTimeout, Duration recommendTimeout) {}
