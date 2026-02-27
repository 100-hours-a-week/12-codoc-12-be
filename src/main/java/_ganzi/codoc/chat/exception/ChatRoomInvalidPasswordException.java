package _ganzi.codoc.chat.exception;

import _ganzi.codoc.global.exception.BaseException;

public class ChatRoomInvalidPasswordException extends BaseException {

    public ChatRoomInvalidPasswordException() {
        super(ChatErrorCode.CHAT_ROOM_INVALID_PASSWORD);
    }
}
