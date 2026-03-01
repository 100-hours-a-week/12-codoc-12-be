package _ganzi.codoc.chatbot.exception;

import _ganzi.codoc.global.exception.BaseException;

public class ChatbotStreamCancelFailedException extends BaseException {

    public ChatbotStreamCancelFailedException() {
        super(ChatbotErrorCode.CHATBOT_STREAM_CANCEL_FAILED);
    }
}
