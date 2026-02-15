package _ganzi.codoc.notification.repository;

import _ganzi.codoc.notification.domain.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {}
