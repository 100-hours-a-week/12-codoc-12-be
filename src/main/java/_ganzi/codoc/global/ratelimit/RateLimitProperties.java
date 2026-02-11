package _ganzi.codoc.global.ratelimit;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.rate-limit")
public record RateLimitProperties(
        @NotNull @Valid Policy chatbotStream, @NotNull @Valid Policy global) {

    public record Policy(
            @NotNull Boolean enabled,
            @NotNull Integer limit,
            @NotNull Duration period,
            @NotNull Integer consumeToken) {}
}
