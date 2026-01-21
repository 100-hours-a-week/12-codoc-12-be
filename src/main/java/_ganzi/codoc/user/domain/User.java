package _ganzi.codoc.user.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 15)
    private String nickname;

    @Column(name = "league_id")
    private Long leagueId;

    @Column(name = "avatar_id")
    private Long avatarId;

    @Column(name = "init_level", length = 20)
    private String initLevel;

    @Column(name = "daily_goal", length = 20)
    private String dailyGoal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status;

    @Column(name = "last_access", nullable = false)
    private Instant lastAccess;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    private User(String nickname, UserStatus status) {
        this.nickname = nickname;
        this.status = status;
    }

    public static User createOnboardingUser(String nickname) {
        return new User(nickname, UserStatus.ONBOARDING);
    }

    public void activate() {
        this.status = UserStatus.ACTIVE;
    }

    public void reviveFromDormant() {
        if (this.status == UserStatus.DORMANT) {
            this.status = UserStatus.ACTIVE;
        }
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
