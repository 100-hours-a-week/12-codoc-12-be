package _ganzi.codoc.chatbot.service;

import _ganzi.codoc.chatbot.domain.ChatbotConversation;
import _ganzi.codoc.chatbot.exception.ChatbotConversationNotFoundException;
import _ganzi.codoc.chatbot.repository.ChatbotConversationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class ChatbotConversationStatusService {

    private final ChatbotConversationRepository chatbotConversationRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markCanceled(Long conversationId) {
        ChatbotConversation conversation =
                chatbotConversationRepository
                        .findById(conversationId)
                        .orElseThrow(ChatbotConversationNotFoundException::new);

        conversation.markCanceled();
    }
}
