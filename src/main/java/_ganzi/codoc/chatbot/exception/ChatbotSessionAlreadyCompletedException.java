package _ganzi.codoc.chatbot.exception;

import _ganzi.codoc.global.exception.BaseException;

public class ChatbotSessionAlreadyCompletedException extends BaseException {

    public ChatbotSessionAlreadyCompletedException() {
        super(ChatbotErrorCode.CHATBOT_SESSION_ALREADY_COMPLETED);
    }
}
