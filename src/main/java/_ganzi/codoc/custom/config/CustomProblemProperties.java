package _ganzi.codoc.custom.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.custom-problem")
public record CustomProblemProperties(
        @Min(1) int maxUploadImageCount,
        @Min(1) long maxUploadImageSizeBytes,
        @NotNull Duration uploadUrlExpiration,
        @NotNull Duration downloadUrlExpiration,
        @NotNull @Valid Async async) {

    public record Async(
            @Min(1) int corePoolSize,
            @Min(1) int maxPoolSize,
            @Min(0) int queueCapacity,
            @NotBlank String threadNamePrefix) {}
}
