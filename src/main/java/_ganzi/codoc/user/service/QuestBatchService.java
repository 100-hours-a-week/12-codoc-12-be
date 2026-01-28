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
        Instant expiresAt = issuedDate.plusDays(1).atStartOfDay(SEOUL).toInstant();
        String sql =
                """
                INSERT INTO user_quest
                    (user_id, quest_id, status, expires_at, issued_date, created_at, updated_at)
                SELECT u.id, q.id, 'IN_PROGRESS', :expiresAt, :issuedDate, NOW(6), NOW(6)
                FROM `user` u
                CROSS JOIN quest q
                WHERE u.status = :activeStatus
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
        Instant expiresAt = issuedDate.plusDays(1).atStartOfDay(SEOUL).toInstant();
        String sql =
                """
                INSERT INTO user_quest
                    (user_id, quest_id, status, expires_at, issued_date, created_at, updated_at)
                SELECT :userId, q.id, 'IN_PROGRESS', :expiresAt, :issuedDate, NOW(6), NOW(6)
                FROM quest q
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
