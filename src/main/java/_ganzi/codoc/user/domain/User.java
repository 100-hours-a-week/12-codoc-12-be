package _ganzi.codoc.user.domain;

import _ganzi.codoc.user.enums.DailyGoal;
import _ganzi.codoc.user.enums.InitLevel;
import _ganzi.codoc.user.enums.UserStatus;
import jakarta.persistence.*;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user")
@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "avatar_id", nullable = false)
    private Avatar avatar;

    @Column(name = "nickname", unique = true, nullable = false, length = 15)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(name = "init_level", length = 50)
    private InitLevel initLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "daily_goal", length = 50)
    private DailyGoal dailyGoal;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private UserStatus status;

    @Column(name = "last_access", nullable = false)
    private Instant lastAccess;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    private User(String nickname, UserStatus status, Avatar avatar) {
        this.nickname = nickname;
        this.status = status;
        this.avatar = avatar;
    }

    public static User createOnboardingUser(String nickname, Avatar avatar) {
        return new User(nickname, UserStatus.ONBOARDING, avatar);
    }

    public void completeOnboarding(InitLevel initLevel, DailyGoal dailyGoal) {
        if (this.status == UserStatus.ONBOARDING) {
            this.initLevel = initLevel;
            this.dailyGoal = dailyGoal;
            this.status = UserStatus.ACTIVE;
        }
    }

    public void markDormant() {
        if (this.status == UserStatus.ACTIVE) {
            this.status = UserStatus.DORMANT;
        }
    }

    public void reviveFromDormant() {
        if (this.status == UserStatus.DORMANT) {
            this.status = UserStatus.ACTIVE;
        }
    }

    public void restoreActiveFromDeleted() {
        if (this.status == UserStatus.DELETED) {
            this.status = UserStatus.ACTIVE;
            this.deletedAt = null;
        }
    }

    public void markDeleted() {
        if (this.status != UserStatus.DELETED) {
            this.status = UserStatus.DELETED;
            this.deletedAt = Instant.now();
        }
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updateAvatar(Avatar avatar) {
        this.avatar = avatar;
    }

    public void updateDailyGoal(DailyGoal dailyGoal) {
        this.dailyGoal = dailyGoal;
    }

    public void touchLastAccess() {
        this.lastAccess = Instant.now();
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
        this.lastAccess = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
