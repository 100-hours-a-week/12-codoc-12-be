package _ganzi.codoc.surprise.domain;

import _ganzi.codoc.global.domain.BaseTimeEntity;
import _ganzi.codoc.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "surprise_event_reward_log")
@Entity
public class SurpriseEventRewardLog extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private SurpriseEvent event;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "reward_type", nullable = false, length = 20)
    private String rewardType;

    @Column(name = "reward_amount", nullable = false)
    private int rewardAmount;

    @Column(name = "idempotency_key", nullable = false, length = 120)
    private String idempotencyKey;

    private SurpriseEventRewardLog(
            SurpriseEvent event, User user, String rewardType, int rewardAmount, String idempotencyKey) {
        this.event = event;
        this.user = user;
        this.rewardType = rewardType;
        this.rewardAmount = rewardAmount;
        this.idempotencyKey = idempotencyKey;
    }

    public static SurpriseEventRewardLog create(
            SurpriseEvent event, User user, String rewardType, int rewardAmount, String idempotencyKey) {
        return new SurpriseEventRewardLog(event, user, rewardType, rewardAmount, idempotencyKey);
    }
}
