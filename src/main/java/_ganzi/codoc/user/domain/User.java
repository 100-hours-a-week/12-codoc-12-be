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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "avatar_id", nullable = false)
    private Avatar avatar;

    @Column(name = "nickname", nullable = false, length = 15)
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

    private User(String nickname, UserStatus status) {
        this.nickname = nickname;
        this.status = status;
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
