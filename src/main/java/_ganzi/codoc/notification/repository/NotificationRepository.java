package _ganzi.codoc.notification.repository;

import _ganzi.codoc.notification.domain.Notification;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query(
            """
            select n
            from Notification n
            where n.user.id = :userId
              and n.createdAt >= :cutoff
              and n.deletedAt is null
            order by n.createdAt desc
            """)
    List<Notification> findRecentByUserId(
            @Param("userId") Long userId, @Param("cutoff") Instant cutoff);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
            """
            update Notification n
               set n.readAt = CURRENT_TIMESTAMP
             where n.user.id = :userId
               and n.id in :notificationIds
               and n.readAt is null
               and n.deletedAt is null
            """)
    void markAsReadByUserIdAndIds(
            @Param("userId") Long userId, @Param("notificationIds") List<Long> notificationIds);

    @Query(
            """
            select case when count(n) > 0 then true else false end
            from Notification n
            where n.user.id = :userId
              and n.readAt is null
              and n.createdAt >= :cutoff
              and n.deletedAt is null
            """)
    boolean existsUnreadRecentByUserId(
            @Param("userId") Long userId, @Param("cutoff") Instant cutoff);
}
