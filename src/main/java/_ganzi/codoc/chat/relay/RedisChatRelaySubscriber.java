package _ganzi.codoc.chat.relay;

import _ganzi.codoc.chat.service.ChatBroadcaster;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
@ConditionalOnProperty(
        prefix = "app.chat.ws",
        name = "relay-enabled",
        havingValue = "true",
        matchIfMissing = true)
public class RedisChatRelaySubscriber implements MessageListener {

    private final ObjectMapper objectMapper;
    private final ChatBroadcaster chatBroadcaster;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String payload = new String(message.getBody(), StandardCharsets.UTF_8);
        ChatRelayEvent event = deserialize(payload);
        if (event == null || event.type() == null) {
            return;
        }

        switch (event.type()) {
            case ROOM_MESSAGE -> broadcastRoomMessage(event);
            case ROOM_LIST_UPDATE -> broadcastRoomListUpdate(event);
            case UNREAD_STATUS_UPDATE -> broadcastUnreadStatusUpdate(event);
            case READ_ACK -> broadcastReadAck(event);
        }
    }

    private ChatRelayEvent deserialize(String payload) {
        try {
            return objectMapper.readValue(payload, ChatRelayEvent.class);
        } catch (Exception exception) {
            log.warn("채팅 릴레이 메시지 파싱에 실패했습니다.", exception);
            return null;
        }
    }

    private void broadcastRoomMessage(ChatRelayEvent event) {
        if (event.roomId() == null || event.message() == null) {
            log.warn("잘못된 채팅방 메시지 릴레이 이벤트입니다. eventId={}", event.eventId());
            return;
        }
        chatBroadcaster.broadcastMessage(event.roomId(), event.message());
    }

    private void broadcastRoomListUpdate(ChatRelayEvent event) {
        if (event.userId() == null || event.roomUpdate() == null) {
            log.warn("잘못된 채팅방 목록 업데이트 릴레이 이벤트입니다. eventId={}", event.eventId());
            return;
        }
        chatBroadcaster.broadcastRoomUpdate(event.userId(), event.roomUpdate());
    }

    private void broadcastUnreadStatusUpdate(ChatRelayEvent event) {
        if (event.userId() == null || event.unreadStatus() == null) {
            log.warn("잘못된 채팅 unread 상태 릴레이 이벤트입니다. eventId={}", event.eventId());
            return;
        }
        chatBroadcaster.broadcastUnreadStatusUpdate(event.userId(), event.unreadStatus());
    }

    private void broadcastReadAck(ChatRelayEvent event) {
        if (event.roomId() == null || event.readAck() == null) {
            log.warn("잘못된 채팅 읽음 확인 릴레이 이벤트입니다. eventId={}", event.eventId());
            return;
        }
        chatBroadcaster.broadcastReadAck(event.roomId(), event.readAck());
    }
}
