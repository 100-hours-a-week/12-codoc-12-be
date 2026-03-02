package _ganzi.codoc.chatbot.exception;

import _ganzi.codoc.global.exception.BaseException;

public class ChatbotConversationNotResumableException extends BaseException {

    public ChatbotConversationNotResumableException() {
        super(ChatbotErrorCode.CHATBOT_CONVERSATION_NOT_RESUMABLE);
    }
}
