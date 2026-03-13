package _ganzi.codoc.chat.dto;

import _ganzi.codoc.chat.domain.ChatRoomParticipant;

public record UserChatRoomDetailResponse(Long roomId, String title, int participantsCount) {

    public static UserChatRoomDetailResponse from(
            ChatRoomParticipant participant, int participantsCount) {
        return new UserChatRoomDetailResponse(
                participant.getChatRoom().getId(), participant.getChatRoom().getTitle(), participantsCount);
    }
}
