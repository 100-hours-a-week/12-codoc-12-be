package _ganzi.codoc.chat.config;

import _ganzi.codoc.auth.domain.AuthUser;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ChatRoomSubscriptionInterceptor implements ChannelInterceptor {

    private static final Pattern ROOM_DESTINATION_PATTERN =
            Pattern.compile("^/sub/chat/rooms/(\\d+)$");

    private final ChatRoomSubscriptionRegistry registry;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            return message;
        }

        StompCommand command = accessor.getCommand();
        if (StompCommand.SUBSCRIBE.equals(command)) {
            handleSubscribe(accessor);
        } else if (StompCommand.UNSUBSCRIBE.equals(command)) {
            handleUnsubscribe(accessor);
        }

        return message;
    }

    private void handleSubscribe(StompHeaderAccessor accessor) {
        Long roomId = extractRoomId(accessor.getDestination());
        if (roomId == null) {
            return;
        }

        Long userId = extractUserId(accessor);
        String sessionId = accessor.getSessionId();
        if (userId == null || sessionId == null) {
            return;
        }

        registry.addSubscription(sessionId, userId, roomId);
    }

    private void handleUnsubscribe(StompHeaderAccessor accessor) {
        Long roomId = extractRoomId(accessor.getDestination());
        if (roomId == null) {
            return;
        }

        String sessionId = accessor.getSessionId();
        if (sessionId == null) {
            return;
        }

        registry.removeSubscription(sessionId, roomId);
    }

    private Long extractRoomId(String destination) {
        if (destination == null) {
            return null;
        }

        Matcher matcher = ROOM_DESTINATION_PATTERN.matcher(destination);
        if (matcher.matches()) {
            return Long.parseLong(matcher.group(1));
        }
        return null;
    }

    private Long extractUserId(StompHeaderAccessor accessor) {
        if (accessor.getUser() instanceof UsernamePasswordAuthenticationToken auth
                && auth.getPrincipal() instanceof AuthUser authUser) {
            return authUser.userId();
        }
        return null;
    }
}
