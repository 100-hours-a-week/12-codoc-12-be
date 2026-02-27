package _ganzi.codoc.chat.exception;

import _ganzi.codoc.global.exception.BaseException;

public class ChatRoomNotFoundException extends BaseException {

    public ChatRoomNotFoundException() {
        super(ChatErrorCode.CHAT_ROOM_NOT_FOUND);
    }
}
