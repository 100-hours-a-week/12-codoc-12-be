package _ganzi.codoc.chat.dto;

import _ganzi.codoc.chat.domain.ChatRoom;
import _ganzi.codoc.chat.domain.ChatRoomParticipant;

public record UserChatRoomDetailResponse(Long roomId, String title, int participantsCount) {

    public static UserChatRoomDetailResponse from(ChatRoomParticipant participant) {
        ChatRoom room = participant.getChatRoom();
        return new UserChatRoomDetailResponse(
                room.getId(), room.getTitle(), room.getParticipantCount());
    }
}
