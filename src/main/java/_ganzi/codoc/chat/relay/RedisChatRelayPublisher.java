package _ganzi.codoc.chat.relay;

import _ganzi.codoc.chat.config.ChatWebSocketProperties;
import _ganzi.codoc.chat.dto.ChatMessageBroadcast;
import _ganzi.codoc.chat.dto.ChatReadAckBroadcast;
import _ganzi.codoc.chat.dto.ChatRoomUpdateBroadcast;
import _ganzi.codoc.chat.dto.ChatUnreadStatusBroadcast;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = "app.chat.ws",
        name = "relay-enabled",
        havingValue = "true",
        matchIfMissing = true)
@Component
public class RedisChatRelayPublisher {

    private final StringRedisTemplate stringRedisTemplate;
    private final JsonMapper jsonMapper;
    private final ChatWebSocketProperties properties;

    public void publishRoomMessage(Long roomId, ChatMessageBroadcast broadcast) {
        publish(ChatRelayEvent.roomMessage(properties.serverId(), roomId, broadcast));
    }

    public void publishRoomUpdate(Long userId, ChatRoomUpdateBroadcast broadcast) {
        publish(ChatRelayEvent.roomListUpdate(properties.serverId(), userId, broadcast));
    }

    public void publishUnreadStatusUpdate(Long userId, ChatUnreadStatusBroadcast broadcast) {
        publish(ChatRelayEvent.unreadStatusUpdate(properties.serverId(), userId, broadcast));
    }

    public void publishReadAck(Long roomId, ChatReadAckBroadcast broadcast) {
        publish(ChatRelayEvent.readAck(properties.serverId(), roomId, broadcast));
    }

    private void publish(ChatRelayEvent event) {
        try {
            String payload = jsonMapper.writeValueAsString(event);
            stringRedisTemplate.convertAndSend(properties.relayChannel(), payload);
        } catch (JacksonException exception) {
            throw new IllegalStateException("채팅 릴레이 이벤트 직렬화에 실패했습니다.", exception);
        }
    }
}
