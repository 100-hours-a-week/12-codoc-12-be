package _ganzi.codoc.chat.scheduler;

import _ganzi.codoc.chat.config.LocalWebSocketSessionRegistry;
import _ganzi.codoc.chat.service.SharedWebSocketStateService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = "app.chat.ws",
        name = "heartbeat-enabled",
        havingValue = "true",
        matchIfMissing = true)
@Component
public class SharedWebSocketHeartbeatScheduler {

    private final LocalWebSocketSessionRegistry localWebSocketSessionRegistry;
    private final SharedWebSocketStateService sharedWebSocketStateService;

    @Scheduled(fixedDelayString = "${app.chat.ws.heartbeat-interval:20s}")
    public void heartbeat() {
        Map<String, Long> snapshot = localWebSocketSessionRegistry.snapshotSessionUsers();
        for (Map.Entry<String, Long> entry : snapshot.entrySet()) {
            sharedWebSocketStateService.touchSession(entry.getKey(), entry.getValue());
        }
    }
}
