package _ganzi.codoc.user.service;

import _ganzi.codoc.user.enums.UserStatus;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class QuestBatchService {

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    private final EntityManager entityManager;

    @Transactional
    public void issueDailyQuests(LocalDate issuedDate) {
        String expireSql =
                """
                UPDATE user_quest
                SET is_expired = true
                WHERE is_expired = false
                  AND expires_at < NOW()
                """;
        entityManager.createNativeQuery(expireSql).executeUpdate();

        Instant expiresAt = issuedDate.plusDays(1).atStartOfDay(SEOUL).toInstant();
        String sql =
                """
                INSERT INTO user_quest
                    (user_id, quest_id, status, expires_at, issued_date, created_at, updated_at)
                SELECT u.id, q.id, 'IN_PROGRESS', :expiresAt, :issuedDate, NOW(6), NOW(6)
                FROM `user` u
                CROSS JOIN quest q
                WHERE u.status = :activeStatus
                  AND q.type = 'DAILY'
                  AND (
                        q.issue_conditions IS NULL
                     OR JSON_EXTRACT(q.issue_conditions, '$.DailyGoal') IS NULL
                     OR JSON_UNQUOTE(JSON_EXTRACT(q.issue_conditions, '$.DailyGoal')) = u.daily_goal
                  )
                ON DUPLICATE KEY UPDATE user_quest.updated_at = NOW(6)
                """;
        entityManager
                .createNativeQuery(sql)
                .setParameter("expiresAt", expiresAt)
                .setParameter("issuedDate", issuedDate)
                .setParameter("activeStatus", UserStatus.ACTIVE.name())
                .executeUpdate();
    }

    @Transactional
    public void issueDailyQuestsForUser(Long userId, LocalDate issuedDate) {
        String expireSql =
                """
                UPDATE user_quest
                SET is_expired = true
                WHERE user_id = :userId
                  AND is_expired = false
                  AND expires_at < NOW()
                """;
        entityManager.createNativeQuery(expireSql).setParameter("userId", userId).executeUpdate();

        Instant expiresAt = issuedDate.plusDays(1).atStartOfDay(SEOUL).toInstant();
        String sql =
                """
                INSERT INTO user_quest
                    (user_id, quest_id, status, expires_at, issued_date, created_at, updated_at)
                SELECT :userId, q.id, 'IN_PROGRESS', :expiresAt, :issuedDate, NOW(6), NOW(6)
                FROM quest q
                JOIN `user` u ON u.id = :userId
                WHERE q.type = 'DAILY'
                  AND (
                        q.issue_conditions IS NULL
                     OR JSON_EXTRACT(q.issue_conditions, '$.DailyGoal') IS NULL
                     OR JSON_UNQUOTE(JSON_EXTRACT(q.issue_conditions, '$.DailyGoal')) = u.daily_goal
                  )
                ON DUPLICATE KEY UPDATE user_quest.updated_at = NOW(6)
                """;
        entityManager
                .createNativeQuery(sql)
                .setParameter("userId", userId)
                .setParameter("expiresAt", expiresAt)
                .setParameter("issuedDate", issuedDate)
                .executeUpdate();
    }

    @Transactional
    public void issueDailyQuests() {
        issueDailyQuests(LocalDate.now(SEOUL));
    }
}
