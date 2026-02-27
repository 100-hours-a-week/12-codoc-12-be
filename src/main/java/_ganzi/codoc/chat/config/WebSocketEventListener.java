package _ganzi.codoc.chat.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Slf4j
@RequiredArgsConstructor
@Component
public class WebSocketEventListener {

    private final ChatRoomSubscriptionRegistry chatRoomSubscriptionRegistry;

    @EventListener
    public void handleWebSocketDisconnect(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();
        log.info("WebSocket session disconnected: {}", sessionId);
        chatRoomSubscriptionRegistry.removeSession(sessionId);
    }
}
