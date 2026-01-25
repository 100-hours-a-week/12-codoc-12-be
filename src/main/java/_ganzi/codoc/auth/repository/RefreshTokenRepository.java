package _ganzi.codoc.auth.repository;

import _ganzi.codoc.auth.domain.RefreshToken;
import _ganzi.codoc.user.domain.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByUser(User user);

    Optional<RefreshToken> findByTokenValue(String tokenValue);

    void deleteByUser(User user);
}
