package _ganzi.codoc.chatbot.exception;

import _ganzi.codoc.global.exception.BaseException;

public class ChatbotStreamRateLimitExceededException extends BaseException {

    public ChatbotStreamRateLimitExceededException() {
        super(ChatbotErrorCode.CHATBOT_STREAM_RATE_LIMIT_EXCEEDED);
    }
}
