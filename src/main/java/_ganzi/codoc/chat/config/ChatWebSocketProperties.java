package _ganzi.codoc.chat.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.chat.ws")
public record ChatWebSocketProperties(
        @NotNull Duration sessionTtl,
        @NotNull Duration presenceTtl,
        @NotNull Duration heartbeatInterval,
        @NotNull Boolean heartbeatEnabled,
        @NotNull Boolean relayEnabled,
        @NotBlank String relayChannel,
        @NotBlank String serverId) {}
