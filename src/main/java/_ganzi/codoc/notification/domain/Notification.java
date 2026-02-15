package _ganzi.codoc.notification.domain;

import _ganzi.codoc.notification.enums.LinkCode;
import _ganzi.codoc.notification.enums.NotificationType;
import _ganzi.codoc.notification.infra.LinkParamsConverter;
import _ganzi.codoc.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Map;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "notification")
@Entity
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private NotificationType type;

    @Column(name = "title", nullable = false, length = 120)
    private String title;

    @Column(name = "body", nullable = false, length = 1000)
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(name = "link_code", nullable = false, length = 30)
    private LinkCode linkCode;

    @Convert(converter = LinkParamsConverter.class)
    @Column(name = "link_params", columnDefinition = "json")
    private Map<String, String> linkParams;

    @Column(name = "read_at")
    private Instant readAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    private Notification(
            User user,
            NotificationType type,
            String title,
            String body,
            LinkCode linkCode,
            Map<String, String> linkParams) {
        this.user = user;
        this.type = type;
        this.title = title;
        this.body = body;
        this.linkCode = linkCode;
        this.linkParams = linkParams == null ? Map.of() : Map.copyOf(linkParams);
    }

    public static Notification create(
            User user,
            NotificationType type,
            String title,
            String body,
            LinkCode linkCode,
            Map<String, String> linkParams) {
        return new Notification(user, type, title, body, linkCode, linkParams);
    }
}
