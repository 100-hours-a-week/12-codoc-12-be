package _ganzi.codoc.notification.repository;

import _ganzi.codoc.notification.domain.UserDevice;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserDeviceRepository extends JpaRepository<UserDevice, Long> {

    Optional<UserDevice> findByUserId(Long userId);

    Optional<UserDevice> findByUserIdAndActiveTrue(Long userId);
}
