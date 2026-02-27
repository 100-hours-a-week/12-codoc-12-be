package _ganzi.codoc.chat.config;

import _ganzi.codoc.auth.domain.AuthUser;
import _ganzi.codoc.auth.jwt.JwtTokenProvider;
import java.security.Principal;
import java.time.Instant;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String TOKEN_EXPIRES_AT = "tokenExpiresAt";

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            return message;
        }

        StompCommand command = accessor.getCommand();

        if (StompCommand.CONNECT.equals(command)) {
            handleConnect(accessor);
        } else if (StompCommand.SEND.equals(command) || StompCommand.SUBSCRIBE.equals(command)) {
            validateTokenExpiry(accessor);
        }

        return message;
    }

    private void handleConnect(StompHeaderAccessor accessor) {
        String token = resolveToken(accessor);
        if (token == null) {
            return;
        }

        AuthUser authUser = jwtTokenProvider.parseUser(token);
        Principal principal =
                new UsernamePasswordAuthenticationToken(authUser, null, authUser.getAuthorities());
        accessor.setUser(principal);

        Instant expiresAt = jwtTokenProvider.getExpiration(token);
        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
        if (sessionAttributes != null) {
            sessionAttributes.put(TOKEN_EXPIRES_AT, expiresAt);
        }
    }

    private void validateTokenExpiry(StompHeaderAccessor accessor) {
        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
        if (sessionAttributes == null) {
            return;
        }

        Instant expiresAt = (Instant) sessionAttributes.get(TOKEN_EXPIRES_AT);
        if (expiresAt != null && Instant.now().isAfter(expiresAt)) {
            throw new MessageDeliveryException("인증 토큰이 만료되었습니다. 재연결이 필요합니다.");
        }
    }

    private String resolveToken(StompHeaderAccessor accessor) {
        String bearer = accessor.getFirstNativeHeader(HttpHeaders.AUTHORIZATION);
        if (bearer != null && bearer.startsWith(BEARER_PREFIX)) {
            return bearer.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}
