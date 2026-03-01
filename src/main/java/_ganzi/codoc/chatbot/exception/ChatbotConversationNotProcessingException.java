package _ganzi.codoc.chatbot.exception;

import _ganzi.codoc.global.exception.BaseException;

public class ChatbotConversationNotProcessingException extends BaseException {

    public ChatbotConversationNotProcessingException() {
        super(ChatbotErrorCode.CHATBOT_CONVERSATION_NOT_PROCESSING);
    }
}
