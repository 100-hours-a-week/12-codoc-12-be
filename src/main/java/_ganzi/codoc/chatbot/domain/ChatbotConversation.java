package _ganzi.codoc.chatbot.domain;

import _ganzi.codoc.chatbot.enums.ChatbotConversationStatus;
import _ganzi.codoc.chatbot.enums.ChatbotParagraphType;
import _ganzi.codoc.chatbot.exception.ChatbotConversationNotProcessingException;
import _ganzi.codoc.global.domain.BaseTimeEntity;
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
    @Column(name = "paragraph_type", nullable = false, length = 20)
    private ChatbotParagraphType paragraphType;

    @Column(name = "is_correct", nullable = false)
    private boolean isCorrect;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ChatbotConversationStatus status;

    private ChatbotConversation(
            ChatbotAttempt attempt,
            String userMessage,
            String aiMessage,
            ChatbotParagraphType paragraphType,
            boolean isCorrect,
            ChatbotConversationStatus status) {
        this.attempt = attempt;
        this.userMessage = userMessage;
        this.aiMessage = aiMessage;
        this.paragraphType = paragraphType;
        this.isCorrect = isCorrect;
        this.status = status;
    }

    public static ChatbotConversation create(
            ChatbotAttempt attempt, String userMessage, ChatbotParagraphType paragraphType) {
        return new ChatbotConversation(
                attempt, userMessage, null, paragraphType, false, ChatbotConversationStatus.PROCESSING);
    }

    public void recordAiResponse(String aiMessage, boolean isCorrect) {
        this.aiMessage = aiMessage;
        this.isCorrect = isCorrect;
        this.status = ChatbotConversationStatus.COMPLETED;
    }

    public void markCanceled() {
        this.status = ChatbotConversationStatus.CANCELED;
    }

    public void validateProcessing() {
        if (this.status != ChatbotConversationStatus.PROCESSING) {
            throw new ChatbotConversationNotProcessingException();
        }
    }
}
