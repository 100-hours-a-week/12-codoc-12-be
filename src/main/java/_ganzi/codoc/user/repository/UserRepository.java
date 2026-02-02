package _ganzi.codoc.user.repository;

import _ganzi.codoc.user.domain.User;
import _ganzi.codoc.user.enums.UserStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {

    List<User> findAllByStatus(UserStatus status);

    List<User> findAllByStatusAndLastAccessBefore(UserStatus status, Instant lastAccess);

    @Query("select u.id from User u where u.status = :status and u.lastAccess < :cutoff")
    List<Long> findIdsByStatusAndLastAccessBefore(
            @Param("status") UserStatus status, @Param("cutoff") Instant cutoff);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
            "update User u set u.status = :toStatus "
                    + "where u.status = :fromStatus and u.lastAccess < :cutoff")
    int bulkUpdateStatusByLastAccessBefore(
            @Param("fromStatus") UserStatus fromStatus,
            @Param("toStatus") UserStatus toStatus,
            @Param("cutoff") Instant cutoff);

    Optional<User> findByIdAndStatus(Long id, UserStatus status);

    boolean existsByNickname(String nickname);

    boolean existsByNicknameAndIdNot(String nickname, Long id);
}
