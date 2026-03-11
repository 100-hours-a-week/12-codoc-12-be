package _ganzi.codoc.notification.repository;

import _ganzi.codoc.notification.domain.NotificationConsumeLog;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationConsumeLogRepository
        extends JpaRepository<NotificationConsumeLog, Long> {

    boolean existsByMessageIdAndChannel(String messageId, String channel);

    Optional<NotificationConsumeLog> findByMessageIdAndChannel(String messageId, String channel);
}
