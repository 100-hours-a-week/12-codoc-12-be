package _ganzi.codoc.surprise.config;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.surprise-event")
public record SurpriseEventProperties(boolean enabled, @NotNull Integer maxRewardCount) {}
