package _ganzi.codoc.chatbot.repository;

import _ganzi.codoc.chatbot.domain.ChatbotAttempt;
import _ganzi.codoc.chatbot.enums.ChatbotAttemptStatus;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatbotAttemptRepository extends JpaRepository<ChatbotAttempt, Long> {

    Optional<ChatbotAttempt> findFirstByUserIdAndProblemIdAndStatusOrderByIdDesc(
            Long userId, Long problemId, ChatbotAttemptStatus status);

    Optional<ChatbotAttempt> findFirstByProblemSessionIdAndStatusOrderByIdDesc(
            Long problemSessionId, ChatbotAttemptStatus status);
}
