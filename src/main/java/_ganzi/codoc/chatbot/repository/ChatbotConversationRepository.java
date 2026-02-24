package _ganzi.codoc.chatbot.repository;

import _ganzi.codoc.chatbot.domain.ChatbotConversation;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
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

    @Query(
            """
            SELECT c FROM ChatbotConversation c
            JOIN FETCH c.attempt a
            JOIN FETCH a.user
            WHERE c.id = :id
            """)
    Optional<ChatbotConversation> findWithAttemptAndUserById(@Param("id") Long id);

    @Query(
            """
            select c
            from ChatbotConversation c
            where c.attempt.id = :attemptId
              and c.id > :cursor
            order by c.id asc
            """)
    List<ChatbotConversation> findConversationListByAttemptId(
            @Param("attemptId") Long attemptId, @Param("cursor") Long cursor, Pageable pageable);
}
