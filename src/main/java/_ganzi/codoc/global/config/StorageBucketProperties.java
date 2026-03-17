package _ganzi.codoc.global.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.storage.bucket")
public record StorageBucketProperties(@NotBlank String images) {}
