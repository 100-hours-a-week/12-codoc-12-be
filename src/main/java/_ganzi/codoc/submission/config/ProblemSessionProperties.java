package _ganzi.codoc.submission.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.problem-session")
public record ProblemSessionProperties(Duration ttl) {}
