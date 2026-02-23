package _ganzi.codoc.chatbot.exception;

import _ganzi.codoc.global.exception.BaseException;

public class ChatbotStreamCancelException extends BaseException {

    public ChatbotStreamCancelException() {
        super(ChatbotErrorCode.CHATBOT_STREAM_CANCEL_FAILED);
    }
}
