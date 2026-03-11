package _ganzi.codoc.notification.domain;

import _ganzi.codoc.global.domain.BaseTimeEntity;
import _ganzi.codoc.notification.enums.NotificationConsumeStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "notification_consume_log")
@Entity
public class NotificationConsumeLog extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "message_id", nullable = false, length = 64)
    private String messageId;

    @Column(name = "channel", nullable = false, length = 20)
    private String channel;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private NotificationConsumeStatus status;

    @Column(name = "last_error", length = 500)
    private String lastError;

    private NotificationConsumeLog(
            String messageId, String channel, NotificationConsumeStatus status) {
        this.messageId = messageId;
        this.channel = channel;
        this.status = status;
    }

    public static NotificationConsumeLog createPending(String messageId, String channel) {
        return new NotificationConsumeLog(messageId, channel, NotificationConsumeStatus.PENDING);
    }

    public static NotificationConsumeLog createDone(String messageId, String channel) {
        return new NotificationConsumeLog(messageId, channel, NotificationConsumeStatus.DONE);
    }

    public void markDone() {
        this.status = NotificationConsumeStatus.DONE;
        this.lastError = null;
    }

    public void markFailed(String errorMessage) {
        this.status = NotificationConsumeStatus.FAILED;
        if (errorMessage == null) {
            this.lastError = null;
            return;
        }
        this.lastError = errorMessage.length() > 500 ? errorMessage.substring(0, 500) : errorMessage;
    }
}
