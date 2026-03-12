package _ganzi.codoc.chat.event;

import _ganzi.codoc.chat.dto.ChatMessageBroadcast;
import _ganzi.codoc.chat.dto.ChatRoomUpdateBroadcast;
import java.util.List;
import java.util.Set;

public record ChatMessageCommittedEvent(
        Long roomId,
        ChatMessageBroadcast roomMessage,
        ChatRoomUpdateBroadcast roomUpdate,
        Set<Long> onlineSubscriberUserIds,
        List<Long> participantUserIds,
        String senderNickname) {

    public ChatMessageCommittedEvent {
        onlineSubscriberUserIds = Set.copyOf(onlineSubscriberUserIds);
        participantUserIds = List.copyOf(participantUserIds);
    }
}
