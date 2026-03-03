package _ganzi.codoc.chatbot.repository;

import _ganzi.codoc.chatbot.domain.ChatbotConversation;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatbotConversationRepository extends JpaRepository<ChatbotConversation, Long> {

    @Query(
            """
            SELECT c FROM ChatbotConversation c
            JOIN FETCH c.problemSession
            WHERE c.id = :conversationId
            """)
    Optional<ChatbotConversation> findByIdWithSession(
            @Param("conversationId") Long conversationId);

    @Query(
            """
            SELECT c FROM ChatbotConversation c
            JOIN FETCH c.problemSession ps
            JOIN FETCH ps.user
            WHERE c.id = :id
            """)
    Optional<ChatbotConversation> findWithSessionAndUserById(@Param("id") Long id);

    @Query(
            """
            SELECT c
            FROM ChatbotConversation c
            WHERE c.problemSession.id = :sessionId
              AND c.id > :cursor
            ORDER BY c.id ASC
            """)
    List<ChatbotConversation> findConversationListBySessionId(
            @Param("sessionId") Long sessionId,
            @Param("cursor") Long cursor,
            Pageable pageable);

    @Query(
            """
            SELECT COUNT(c)
            FROM ChatbotConversation c
            JOIN c.problemSession ps
            WHERE ps.user.id = :userId
              AND c.createdAt BETWEEN :startAt AND :endAt
            """)
    long countByUserIdAndCreatedAtBetween(
            @Param("userId") Long userId,
            @Param("startAt") Instant startAt,
            @Param("endAt") Instant endAt);

    @Query(
            """
            SELECT c
            FROM ChatbotConversation c
            JOIN FETCH c.problemSession ps
            JOIN FETCH ps.problem p
            WHERE ps.user.id = :userId
              AND c.createdAt BETWEEN :startAt AND :endAt
            ORDER BY c.createdAt ASC
            """)
    List<ChatbotConversation> findAllByUserIdAndCreatedAtBetweenWithSession(
            @Param("userId") Long userId,
            @Param("startAt") Instant startAt,
            @Param("endAt") Instant endAt);
}
