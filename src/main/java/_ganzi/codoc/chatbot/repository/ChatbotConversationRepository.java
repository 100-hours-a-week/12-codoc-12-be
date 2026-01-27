package _ganzi.codoc.chatbot.repository;

import _ganzi.codoc.chatbot.domain.ChatbotConversation;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatbotConversationRepository extends JpaRepository<ChatbotConversation, Long> {

    @Query(
            """
            select conversation from ChatbotConversation conversation join fetch conversation.attempt
             where conversation.id = :conversationId
            """)
    Optional<ChatbotConversation> findByIdWithAttempt(@Param("conversationId") Long conversationId);
}
