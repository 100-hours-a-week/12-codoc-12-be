package _ganzi.codoc.chatbot.domain;

import _ganzi.codoc.global.domain.BaseTimeEntity;
import _ganzi.codoc.problem.enums.ParagraphType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "chatbot_conversation")
@Entity
public class ChatbotConversation extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attempt_id", nullable = false)
    private ChatbotAttempt attempt;

    @Column(name = "user_message", nullable = false, length = 500)
    private String userMessage;

    @Lob
    @Column(name = "ai_message", columnDefinition = "TEXT")
    private String aiMessage;

    @Enumerated(EnumType.STRING)
    @Column(name = "node", nullable = false, length = 20)
    private ParagraphType node;

    @Column(name = "is_correct", nullable = false)
    private boolean isCorrect;

    private ChatbotConversation(
            ChatbotAttempt attempt,
            String userMessage,
            String aiMessage,
            ParagraphType node,
            boolean isCorrect) {
        this.attempt = attempt;
        this.userMessage = userMessage;
        this.aiMessage = aiMessage;
        this.node = node;
        this.isCorrect = isCorrect;
    }

    public static ChatbotConversation create(
            ChatbotAttempt attempt, String userMessage, ParagraphType node) {
        return new ChatbotConversation(attempt, userMessage, null, node, false);
    }

    public void recordAiResponse(String aiMessage, boolean isCorrect) {
        this.aiMessage = aiMessage;
        this.isCorrect = isCorrect;
    }
}
