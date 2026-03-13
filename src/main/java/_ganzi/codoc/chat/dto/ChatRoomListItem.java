package _ganzi.codoc.chat.dto;

import java.time.Instant;

public record ChatRoomListItem(
        Long roomId,
        String title,
        boolean hasPassword,
        int participantCount,
        int maxParticipants,
        Instant lastMessageAt) {

    public static ChatRoomListItem from(ChatRoomListQueryResult view, int maxParticipants) {
        return new ChatRoomListItem(
                view.roomId(),
                view.title(),
                view.hasPassword(),
                Math.toIntExact(view.participantCount()),
                maxParticipants,
                view.lastMessageAt());
    }
}
