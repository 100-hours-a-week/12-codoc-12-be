package _ganzi.codoc.chat.event;

import _ganzi.codoc.auth.domain.AuthUser;
import _ganzi.codoc.chat.config.LocalWebSocketRoomSubscriptionRegistry;
import _ganzi.codoc.chat.config.LocalWebSocketSessionRegistry;
import _ganzi.codoc.chat.service.SharedWebSocketStateService;
import java.security.Principal;
import java.util.Set;
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

    private final LocalWebSocketRoomSubscriptionRegistry localWebSocketRoomSubscriptionRegistry;
    private final LocalWebSocketSessionRegistry localWebSocketSessionRegistry;
    private final SharedWebSocketStateService sharedWebSocketStateService;

    @EventListener
    public void handleWebSocketConnect(SessionConnectedEvent event) {
        Principal principal = event.getUser();
        if (principal instanceof UsernamePasswordAuthenticationToken auth
                && auth.getPrincipal() instanceof AuthUser authUser) {
            String sessionId = event.getMessage().getHeaders().get("simpSessionId", String.class);
            if (sessionId != null) {
                localWebSocketSessionRegistry.addSession(sessionId, authUser.userId());
                sharedWebSocketStateService.registerSession(sessionId, authUser.userId());
                log.info("WebSocket session connected: {} userId: {}", sessionId, authUser.userId());
            }
        }
    }

    @EventListener
    public void handleWebSocketDisconnect(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();
        log.info("WebSocket session disconnected: {}", sessionId);
        Long userId = localWebSocketSessionRegistry.findUserId(sessionId);
        Set<Long> roomIds = localWebSocketRoomSubscriptionRegistry.getSessionRoomIds(sessionId);
        localWebSocketRoomSubscriptionRegistry.removeSession(sessionId);
        localWebSocketSessionRegistry.removeSession(sessionId);
        sharedWebSocketStateService.removeSession(sessionId, userId, roomIds);
    }
}
