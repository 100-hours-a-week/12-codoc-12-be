package _ganzi.codoc.user.domain;

import _ganzi.codoc.global.domain.BaseTimeEntity;
import _ganzi.codoc.user.enums.QuestStatus;
import jakarta.persistence.*;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "user_quest",
        indexes = {@Index(name = "idx_user_quest_user_status", columnList = "user_id,status")})
@Entity
public class UserQuest extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quest_id", nullable = false)
    private Quest quest;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private QuestStatus status;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    private UserQuest(User user, Quest quest, QuestStatus status, Instant expiresAt) {
        this.user = user;
        this.quest = quest;
        this.status = status;
        this.expiresAt = expiresAt;
    }

    public static UserQuest create(User user, Quest quest, Instant expiresAt) {
        return new UserQuest(user, quest, QuestStatus.IN_PROGRESS, expiresAt);
    }

    public void markCompleted() {
        if (this.status == QuestStatus.IN_PROGRESS) {
            this.status = QuestStatus.COMPLETED;
        }
    }

    public void markClaimed() {
        if (this.status == QuestStatus.COMPLETED) {
            this.status = QuestStatus.CLAIMED;
        }
    }

    public void markExpired() {
        if (this.status != QuestStatus.CLAIMED) {
            this.status = QuestStatus.EXPIRED;
        }
    }
}
