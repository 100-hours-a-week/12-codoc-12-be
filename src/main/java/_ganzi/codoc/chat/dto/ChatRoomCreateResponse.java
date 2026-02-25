package _ganzi.codoc.chat.dto;

import _ganzi.codoc.chat.domain.ChatRoom;

public record ChatRoomCreateResponse(Long roomId) {

    public static ChatRoomCreateResponse from(ChatRoom chatRoom) {
        return new ChatRoomCreateResponse(chatRoom.getId());
    }
}
