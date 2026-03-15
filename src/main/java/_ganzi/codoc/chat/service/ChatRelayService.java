package _ganzi.codoc.chat.service;

import _ganzi.codoc.chat.dto.ChatMessageBroadcast;
import _ganzi.codoc.chat.dto.ChatReadAckBroadcast;
import _ganzi.codoc.chat.dto.ChatRoomUpdateBroadcast;
import _ganzi.codoc.chat.dto.ChatUnreadStatusBroadcast;
import _ganzi.codoc.chat.relay.RedisChatRelayPublisher;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class ChatRelayService {

    private final ObjectProvider<RedisChatRelayPublisher> redisChatRelayPublisherProvider;
    private final ChatBroadcaster chatBroadcaster;

    public void relayRoomMessage(Long roomId, ChatMessageBroadcast broadcast) {
        publishOrFallback(
                publisher -> publisher.publishRoomMessage(roomId, broadcast),
                () -> chatBroadcaster.broadcastMessage(roomId, broadcast),
                "roomId=" + roomId);
    }

    public void relayRoomUpdate(Long userId, ChatRoomUpdateBroadcast broadcast) {
        publishOrFallback(
                publisher -> publisher.publishRoomUpdate(userId, broadcast),
                () -> chatBroadcaster.broadcastRoomUpdate(userId, broadcast),
                "userId=" + userId);
    }

    public void relayUnreadStatusUpdate(Long userId, ChatUnreadStatusBroadcast broadcast) {
        publishOrFallback(
                publisher -> publisher.publishUnreadStatusUpdate(userId, broadcast),
                () -> chatBroadcaster.broadcastUnreadStatusUpdate(userId, broadcast),
                "userId=" + userId);
    }

    public void relayReadAck(Long roomId, ChatReadAckBroadcast broadcast) {
        publishOrFallback(
                publisher -> publisher.publishReadAck(roomId, broadcast),
                () -> chatBroadcaster.broadcastReadAck(roomId, broadcast),
                "roomId=" + roomId);
    }

    private void publishOrFallback(
            Consumer<RedisChatRelayPublisher> publishAction,
            Runnable localFallbackAction,
            String target) {
        RedisChatRelayPublisher publisher = redisChatRelayPublisherProvider.getIfAvailable();
        if (publisher == null) {
            localFallbackAction.run();
            return;
        }

        try {
            publishAction.accept(publisher);
        } catch (RuntimeException exception) {
            log.warn("채팅 릴레이 발행 실패로 로컬 브로드캐스트로 대체합니다. target={}", target, exception);
            localFallbackAction.run();
        }
    }
}
