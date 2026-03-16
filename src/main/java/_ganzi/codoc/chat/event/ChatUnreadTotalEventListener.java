package _ganzi.codoc.chat.event;

import _ganzi.codoc.chat.dto.ChatReadAckBroadcast;
import _ganzi.codoc.chat.dto.ChatUnreadStatusBroadcast;
import _ganzi.codoc.chat.service.ChatRelayService;
import _ganzi.codoc.chat.service.ChatUnreadCountService;
import _ganzi.codoc.chat.service.SharedWebSocketStateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@RequiredArgsConstructor
@Component
public class ChatUnreadTotalEventListener {

    private final ChatUnreadCountService chatUnreadCountService;
    private final SharedWebSocketStateService sharedWebSocketStateService;
    private final ChatRelayService chatRelayService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleChatUnreadTotalAdjusted(ChatUnreadTotalAdjustedEvent event) {
        long totalUnreadCount =
                chatUnreadCountService.decreaseTotalUnreadCount(event.userId(), event.delta());

        if (!sharedWebSocketStateService.isConnected(event.userId())) {
            return;
        }

        chatRelayService.relayUnreadStatusUpdate(
                event.userId(), ChatUnreadStatusBroadcast.of(totalUnreadCount));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleChatUnreadTotalSyncRequested(ChatUnreadTotalSyncRequestedEvent event) {
        long totalUnreadCount = chatUnreadCountService.syncTotalUnreadCount(event.userId());

        if (!sharedWebSocketStateService.isConnected(event.userId())) {
            return;
        }

        chatRelayService.relayUnreadStatusUpdate(
                event.userId(), ChatUnreadStatusBroadcast.of(totalUnreadCount));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleChatReadAckCommitted(ChatReadAckCommittedEvent event) {
        chatRelayService.relayReadAck(
                event.roomId(),
                new ChatReadAckBroadcast(
                        event.userId(), event.previousLastReadMessageId(), event.lastReadMessageId()));
    }
}
