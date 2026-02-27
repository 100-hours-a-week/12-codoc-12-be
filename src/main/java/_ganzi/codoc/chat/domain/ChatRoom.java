package _ganzi.codoc.chat.domain;

import _ganzi.codoc.global.domain.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "chat_room")
@Entity
public class ChatRoom extends BaseTimeEntity {

    private static final int LAST_MESSAGE_PREVIEW_MAX_LENGTH = 50;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "password", length = 100)
    private String password;

    @Column(name = "participant_count", nullable = false)
    private int participantCount;

    @Column(name = "last_message_id", nullable = false)
    private long lastMessageId;

    @Column(name = "last_message_preview", nullable = false, length = 50)
    private String lastMessagePreview;

    @Column(name = "last_message_at", nullable = false)
    private Instant lastMessageAt;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    private ChatRoom(String title, String password, String lastMessage, Instant lastMessageAt) {
        this.title = title;
        this.password = password;
        this.participantCount = 1;
        this.lastMessageId = 0;
        this.lastMessagePreview = toPreview(lastMessage);
        this.lastMessageAt = lastMessageAt;
        this.isDeleted = false;
    }

    public static ChatRoom create(
            String title, String password, String lastMessage, Instant lastMessageAt) {
        return new ChatRoom(title, password, lastMessage, lastMessageAt);
    }

    public void applyLastMessage(ChatMessage message) {
        if (!message.getType().isSenderIdRequired()) {
            return;
        }

        if (message.getId() == null
                || message.getCreatedAt() == null
                || message.getContent() == null
                || message.getContent().isBlank()) {
            throw new IllegalArgumentException("Message has a invalid value");
        }

        this.lastMessageId = message.getId();
        this.lastMessageAt = message.getCreatedAt();
        this.lastMessagePreview = toPreview(message.getContent());
    }

    public boolean hasPassword() {
        return password != null;
    }

    public void incrementParticipantCount() {
        this.participantCount++;
    }

    public void decrementParticipantCount() {
        this.participantCount--;
    }

    private String toPreview(String originalContent) {
        int codePointCount = originalContent.codePointCount(0, originalContent.length());
        if (codePointCount <= LAST_MESSAGE_PREVIEW_MAX_LENGTH) {
            return originalContent;
        }

        int endIndex = originalContent.offsetByCodePoints(0, LAST_MESSAGE_PREVIEW_MAX_LENGTH);
        return originalContent.substring(0, endIndex);
    }
}
