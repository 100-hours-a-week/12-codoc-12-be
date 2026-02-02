package _ganzi.codoc.chatbot.exception;

import _ganzi.codoc.global.exception.BaseException;

public class ChatbotStreamEventException extends BaseException {

    public ChatbotStreamEventException() {
        super(ChatbotErrorCode.CHATBOT_STREAM_EVENT_FAILED);
    }
}
