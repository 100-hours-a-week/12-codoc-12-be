package _ganzi.codoc.global.alert;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DiscordAlertService {

    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(5);

    private final HttpClient httpClient =
            HttpClient.newBuilder().connectTimeout(REQUEST_TIMEOUT).build();
    private final String webhookUrl;

    public DiscordAlertService(@Value("${app.alert.discord.webhook-url:}") String webhookUrl) {
        this.webhookUrl = webhookUrl;
    }

    public void send(String content) {
        if (webhookUrl == null || webhookUrl.isBlank() || content == null || content.isBlank()) {
            return;
        }
        try {
            String body = "{\"content\":\"" + escapeJson(content) + "\"}";
            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(URI.create(webhookUrl))
                            .timeout(REQUEST_TIMEOUT)
                            .header("Content-Type", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                            .build();
            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() >= 300) {
                log.warn(
                        "discord alert webhook failed. status={}, body={}",
                        response.statusCode(),
                        response.body());
            }
        } catch (Exception exception) {
            log.warn("discord alert webhook call failed", exception);
        }
    }

    private String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }
}
