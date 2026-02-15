package _ganzi.codoc.notification.domain;

import _ganzi.codoc.global.domain.BaseTimeEntity;
import _ganzi.codoc.notification.enums.DevicePlatform;
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
@Table(name = "user_device")
@Entity
public class UserDevice extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "platform", nullable = false, length = 20)
    private DevicePlatform platform;

    @Column(name = "push_token", nullable = false, length = 512)
    private String pushToken;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    private UserDevice(User user, DevicePlatform platform, String pushToken) {
        this.user = user;
        this.platform = platform;
        this.pushToken = pushToken;
        this.active = true;
    }

    public static UserDevice create(User user, DevicePlatform platform, String pushToken) {
        return new UserDevice(user, platform, pushToken);
    }
}
