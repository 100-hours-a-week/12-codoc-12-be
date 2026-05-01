package _ganzi.codoc.problem.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.cache.problem-content")
public record ProblemContentCacheProperties(@NotBlank String problemContent, @NotBlank String problemNull) {}
