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

    @Query(
            """
            select count(c)
            from ChatbotConversation c
            join c.attempt a
            join a.problemSession ps
            where ps.user.id = :userId
              and c.createdAt between :startAt and :endAt
            """)
    long countByUserIdAndCreatedAtBetween(
            @Param("userId") Long userId,
            @Param("startAt") Instant startAt,
            @Param("endAt") Instant endAt);

    @Query(
            """
            select c
            from ChatbotConversation c
            join fetch c.attempt a
            join fetch a.problem p
            join a.problemSession ps
            where ps.user.id = :userId
              and c.createdAt between :startAt and :endAt
            order by c.createdAt asc
            """)
    List<ChatbotConversation> findAllByUserIdAndCreatedAtBetweenWithAttempt(
            @Param("userId") Long userId,
            @Param("startAt") Instant startAt,
            @Param("endAt") Instant endAt);
}
