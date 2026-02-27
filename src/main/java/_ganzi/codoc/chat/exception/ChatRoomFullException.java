package _ganzi.codoc.chat.exception;

import _ganzi.codoc.global.exception.BaseException;

public class ChatRoomFullException extends BaseException {

    public ChatRoomFullException() {
        super(ChatErrorCode.CHAT_ROOM_FULL);
    }
}
