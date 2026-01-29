package _ganzi.codoc.chatbot.exception;

import _ganzi.codoc.global.exception.BaseException;

public class ChatbotConversationNoPermissionException extends BaseException {

    public ChatbotConversationNoPermissionException() {
        super(ChatbotErrorCode.CHATBOT_CONVERSATION_NO_PERMISSION);
    }
}
