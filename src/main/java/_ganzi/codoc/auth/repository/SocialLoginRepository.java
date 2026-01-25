package _ganzi.codoc.auth.repository;

import _ganzi.codoc.auth.domain.SocialLogin;
import _ganzi.codoc.auth.enums.SocialProvider;
import _ganzi.codoc.user.domain.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SocialLoginRepository extends JpaRepository<SocialLogin, Long> {

    Optional<SocialLogin> findByProviderNameAndProviderUserId(
            SocialProvider providerName, String providerUserId);

    List<SocialLogin> findAllByUser(User user);

    void deleteByUser(User user);
}
