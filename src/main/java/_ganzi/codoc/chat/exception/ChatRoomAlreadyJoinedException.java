package _ganzi.codoc.chat.exception;

import _ganzi.codoc.global.exception.BaseException;

public class ChatRoomAlreadyJoinedException extends BaseException {

    public ChatRoomAlreadyJoinedException() {
        super(ChatErrorCode.CHAT_ROOM_ALREADY_JOINED);
    }
}
