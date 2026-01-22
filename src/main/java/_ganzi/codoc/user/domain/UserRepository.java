package _ganzi.codoc.user.domain;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    List<User> findAllByStatus(UserStatus status);

    List<User> findAllByStatusAndLastAccessBefore(UserStatus status, Instant lastAccess);

    Optional<User> findByIdAndStatus(Long id, UserStatus status);

    boolean existsByNicknameAndIdNot(String nickname, Long id);
}
