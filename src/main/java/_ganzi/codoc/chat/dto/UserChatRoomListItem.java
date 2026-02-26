package _ganzi.codoc.chat.dto;

import _ganzi.codoc.chat.domain.ChatRoom;
import _ganzi.codoc.chat.domain.ChatRoomParticipant;
import java.time.Instant;

public record UserChatRoomListItem(
        Long roomId,
        String title,
        int participantsCount,
        String lastMessagePreview,
        Instant lastMessageAt,
        long unreadCount) {

    public static UserChatRoomListItem from(ChatRoomParticipant participant, long unreadCount) {
        ChatRoom room = participant.getChatRoom();
        return new UserChatRoomListItem(
                room.getId(),
                room.getTitle(),
                room.getParticipantCount(),
                room.getLastMessagePreview(),
                room.getLastMessageAt(),
                unreadCount);
    }
}
