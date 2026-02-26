package _ganzi.codoc.chatbot.domain;

import _ganzi.codoc.chatbot.enums.ChatbotAttemptStatus;
import _ganzi.codoc.chatbot.enums.ChatbotParagraphType;
import _ganzi.codoc.global.domain.BaseTimeEntity;
import _ganzi.codoc.problem.domain.Problem;
import _ganzi.codoc.submission.domain.ProblemSession;
import _ganzi.codoc.user.domain.User;
import jakarta.persistence.*;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "chatbot_attempt")
@Entity
public class ChatbotAttempt extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    @Enumerated(EnumType.STRING)
    @Column(name = "paragraph_type", nullable = false, length = 20)
    private ChatbotParagraphType currentParagraphType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ChatbotAttemptStatus status;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_session_id")
    private ProblemSession problemSession;

    private ChatbotAttempt(
            User user,
            Problem problem,
            ChatbotParagraphType currentParagraphType,
            ChatbotAttemptStatus status,
            Instant expiresAt,
            ProblemSession problemSession) {
        this.user = user;
        this.problem = problem;
        this.currentParagraphType = currentParagraphType;
        this.status = status;
        this.expiresAt = expiresAt;
        this.problemSession = problemSession;
    }

    public static ChatbotAttempt create(User user, Problem problem, ProblemSession problemSession) {
        return new ChatbotAttempt(
                user,
                problem,
                ChatbotParagraphType.getInitialType(),
                ChatbotAttemptStatus.ACTIVE,
                problemSession.getExpiresAt(),
                problemSession);
    }

    public void advanceToNextParagraph() {
        this.currentParagraphType = this.currentParagraphType.next();
    }
}
