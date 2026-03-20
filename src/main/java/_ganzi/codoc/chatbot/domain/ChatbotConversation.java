package _ganzi.codoc.chatbot.domain;

import _ganzi.codoc.chatbot.enums.ChatbotConversationStatus;
import _ganzi.codoc.chatbot.enums.ChatbotMessageType;
import _ganzi.codoc.chatbot.enums.ChatbotParagraphType;
import _ganzi.codoc.chatbot.exception.ChatbotConversationNotProcessingException;
import _ganzi.codoc.chatbot.exception.ChatbotConversationNotResumableException;
import _ganzi.codoc.global.domain.BaseTimeEntity;
import _ganzi.codoc.submission.domain.ProblemSession;
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
    @JoinColumn(name = "problem_session_id", nullable = false)
    private ProblemSession problemSession;

    @Column(name = "user_message", nullable = false, length = 500)
    private String userMessage;

    @Lob
    @Column(name = "ai_message", columnDefinition = "TEXT")
    private String aiMessage;

    @Enumerated(EnumType.STRING)
    @Column(name = "paragraph_type", nullable = false, length = 20)
    private ChatbotParagraphType paragraphType;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false, length = 20)
    private ChatbotMessageType messageType;

    @Column(name = "is_correct", nullable = false)
    private boolean isCorrect;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ChatbotConversationStatus status;

    private ChatbotConversation(
            ProblemSession problemSession,
            String userMessage,
            String aiMessage,
            ChatbotParagraphType paragraphType,
            ChatbotMessageType messageType,
            boolean isCorrect,
            ChatbotConversationStatus status) {
        this.problemSession = problemSession;
        this.userMessage = userMessage;
        this.aiMessage = aiMessage;
        this.paragraphType = paragraphType;
        this.messageType = messageType;
        this.isCorrect = isCorrect;
        this.status = status;
    }

    public static ChatbotConversation create(
            ProblemSession problemSession,
            String userMessage,
            ChatbotParagraphType paragraphType,
            ChatbotMessageType messageType) {
        return new ChatbotConversation(
                problemSession,
                userMessage,
                null,
                paragraphType,
                messageType,
                false,
                ChatbotConversationStatus.PROCESSING);
    }

    public void recordAiResponse(String aiMessage, boolean isCorrect) {
        this.aiMessage = aiMessage;
        this.isCorrect = isCorrect;
        this.status = ChatbotConversationStatus.COMPLETED;
    }

    public void markCanceled() {
        this.status = ChatbotConversationStatus.CANCELED;
    }

    public void markDisconnected() {
        if (this.status != ChatbotConversationStatus.PROCESSING) {
            return;
        }
        this.status = ChatbotConversationStatus.DISCONNECTED;
    }

    public void markFailed() {
        if (this.status != ChatbotConversationStatus.PROCESSING) {
            return;
        }
        this.status = ChatbotConversationStatus.FAILED;
    }

    public void validateProcessing() {
        if (this.status != ChatbotConversationStatus.PROCESSING) {
            throw new ChatbotConversationNotProcessingException();
        }
    }

    public void prepareResume() {
        boolean hasAiMessage = this.aiMessage != null && !this.aiMessage.isBlank();
        boolean resumableDisconnected = this.status == ChatbotConversationStatus.DISCONNECTED;
        boolean resumableProcessing =
                this.status == ChatbotConversationStatus.PROCESSING && !hasAiMessage;

        if (!resumableDisconnected && !resumableProcessing) {
            throw new ChatbotConversationNotResumableException();
        }
        this.status = ChatbotConversationStatus.PROCESSING;
    }
}
