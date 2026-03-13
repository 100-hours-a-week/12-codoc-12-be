package _ganzi.codoc.chat.dto;

import _ganzi.codoc.chat.domain.ChatRoom;
import java.time.Instant;

public record UserChatRoomListItem(
        Long roomId,
        String title,
        int participantsCount,
        String lastMessagePreview,
        Instant lastMessageAt,
        long unreadCount) {

    public static UserChatRoomListItem from(UserChatRoomListQueryResult view, long unreadCount) {
        return new UserChatRoomListItem(
                view.roomId(),
                view.title(),
                Math.toIntExact(view.participantsCount()),
                ChatRoom.toListPreview(view.lastMessagePreview()),
                view.lastMessageAt(),
                unreadCount);
    }
}
