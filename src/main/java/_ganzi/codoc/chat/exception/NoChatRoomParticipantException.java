package _ganzi.codoc.chat.exception;

import _ganzi.codoc.global.exception.BaseException;

public class NoChatRoomParticipantException extends BaseException {

    public NoChatRoomParticipantException() {
        super(ChatErrorCode.NO_CHAT_ROOM_PARTICIPANT);
    }
}
