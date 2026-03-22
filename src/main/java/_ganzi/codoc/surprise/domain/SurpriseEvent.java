package _ganzi.codoc.surprise.domain;

import _ganzi.codoc.global.domain.BaseTimeEntity;
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
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "surprise_event")
@Entity
public class SurpriseEvent extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quiz_pool_id", nullable = false)
    private SurpriseQuizPool quizPool;

    @Column(name = "event_week_key", length = 8)
    private String eventWeekKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SurpriseEventStatus status;

    @Column(name = "starts_at", nullable = false)
    private Instant startsAt;

    @Column(name = "ends_at", nullable = false)
    private Instant endsAt;

    @Column(name = "settled_at")
    private Instant settledAt;

    private SurpriseEvent(
            SurpriseQuizPool quizPool,
            String eventWeekKey,
            SurpriseEventStatus status,
            Instant startsAt,
            Instant endsAt) {
        this.quizPool = quizPool;
        this.eventWeekKey = eventWeekKey;
        this.status = status;
        this.startsAt = startsAt;
        this.endsAt = endsAt;
    }

    public static SurpriseEvent schedule(
            SurpriseQuizPool quizPool, String eventWeekKey, Instant startsAt, Instant endsAt) {
        return new SurpriseEvent(
                quizPool, eventWeekKey, SurpriseEventStatus.SCHEDULED, startsAt, endsAt);
    }

    public boolean isOpenAt(Instant now) {
        return status == SurpriseEventStatus.OPEN && !now.isBefore(startsAt) && now.isBefore(endsAt);
    }

    public boolean isEnded(Instant now) {
        return !now.isBefore(endsAt);
    }

    public boolean isSettled() {
        return settledAt != null;
    }

    public void close() {
        this.status = SurpriseEventStatus.CLOSED;
    }

    public void open() {
        this.status = SurpriseEventStatus.OPEN;
    }

    public void markSettled(Instant now) {
        this.settledAt = now;
    }
}
