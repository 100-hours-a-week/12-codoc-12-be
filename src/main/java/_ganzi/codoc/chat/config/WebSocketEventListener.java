package _ganzi.codoc.chat.config;

import _ganzi.codoc.auth.domain.AuthUser;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Slf4j
@RequiredArgsConstructor
@Component
public class WebSocketEventListener {

    private final ChatRoomSubscriptionRegistry chatRoomSubscriptionRegistry;
    private final WebSocketSessionRegistry webSocketSessionRegistry;

    @EventListener
    public void handleWebSocketConnect(SessionConnectedEvent event) {
        Principal principal = event.getUser();
        if (principal instanceof UsernamePasswordAuthenticationToken auth
                && auth.getPrincipal() instanceof AuthUser authUser) {
            String sessionId = event.getMessage().getHeaders().get("simpSessionId", String.class);
            if (sessionId != null) {
                webSocketSessionRegistry.addSession(sessionId, authUser.userId());
                log.info("WebSocket session connected: {} userId: {}", sessionId, authUser.userId());
            }
        }
    }

    @EventListener
    public void handleWebSocketDisconnect(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();
        log.info("WebSocket session disconnected: {}", sessionId);
        chatRoomSubscriptionRegistry.removeSession(sessionId);
        webSocketSessionRegistry.removeSession(sessionId);
    }
}
