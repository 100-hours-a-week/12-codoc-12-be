package _ganzi.codoc.notification.repository;

import _ganzi.codoc.notification.domain.UserNotificationPreference;
import _ganzi.codoc.notification.enums.NotificationType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserNotificationPreferenceRepository
        extends JpaRepository<UserNotificationPreference, Long> {

    List<UserNotificationPreference> findAllByUserId(Long userId);

    Optional<UserNotificationPreference> findByUserIdAndType(Long userId, NotificationType type);
}
