package _ganzi.codoc.chatbot.repository;

import _ganzi.codoc.chatbot.domain.ChatbotConversation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatbotConversationRepository extends JpaRepository<ChatbotConversation, Long> {}
