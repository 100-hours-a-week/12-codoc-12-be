package _ganzi.codoc.notification.domain;

import _ganzi.codoc.global.domain.BaseTimeEntity;
import _ganzi.codoc.notification.enums.NotificationType;
import _ganzi.codoc.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user_notification_preference")
@Entity
public class UserNotificationPreference extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private NotificationType type;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    private UserNotificationPreference(User user, NotificationType type, boolean enabled) {
        this.user = user;
        this.type = type;
        this.enabled = enabled;
    }

    public static UserNotificationPreference create(
            User user, NotificationType type, boolean enabled) {
        return new UserNotificationPreference(user, type, enabled);
    }

    public void updateEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
