package _ganzi.codoc.chatbot.repository;

import _ganzi.codoc.chatbot.domain.ChatbotAttempt;
import _ganzi.codoc.chatbot.enums.ChatbotAttemptStatus;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatbotAttemptRepository extends JpaRepository<ChatbotAttempt, Long> {

    Optional<ChatbotAttempt> findFirstByUserIdAndProblemIdAndStatusOrderByIdDesc(
            Long userId, Long problemId, ChatbotAttemptStatus status);

    @Query(
            """
            select a
            from ChatbotAttempt a
            where a.user.id = :userId
              and a.problem.id = :problemId
              and a.status = _ganzi.codoc.chatbot.enums.ChatbotAttemptStatus.ACTIVE
              and a.expiresAt > :now
            order by a.id desc
            """)
    Optional<ChatbotAttempt> findCurrentActiveAttemptByUserIdAndProblemId(
            @Param("userId") Long userId,
            @Param("problemId") Long problemId,
            @Param("now") Instant now);
}
