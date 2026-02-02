package _ganzi.codoc.auth.repository;

import _ganzi.codoc.auth.domain.RefreshToken;
import _ganzi.codoc.user.domain.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByUser(User user);

    Optional<RefreshToken> findByTokenValue(String tokenValue);

    void deleteByTokenValue(String tokenValue);

    void deleteByUser(User user);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from RefreshToken r where r.user.id in :userIds")
    void deleteByUserIdIn(@Param("userIds") List<Long> userIds);
}
