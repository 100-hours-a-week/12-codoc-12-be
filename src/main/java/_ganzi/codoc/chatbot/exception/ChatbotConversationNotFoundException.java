package _ganzi.codoc.chatbot.exception;

import _ganzi.codoc.global.exception.BaseException;

public class ChatbotConversationNotFoundException extends BaseException {

    public ChatbotConversationNotFoundException() {
        super(ChatbotErrorCode.CHATBOT_CONVERSATION_NOT_FOUND);
    }
}
