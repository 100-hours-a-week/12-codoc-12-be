package _ganzi.codoc.problem.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.cache.problem-content.invalidation")
public record ProblemContentCacheInvalidationProperties(
        @NotNull Boolean enabled, @NotBlank String channel, @NotBlank String serverId) {}
