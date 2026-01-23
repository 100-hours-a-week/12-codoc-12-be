package _ganzi.codoc.user.domain;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AvatarRepository extends JpaRepository<Avatar, Integer> {

    Optional<Avatar> findByIsDefaultTrue();
}
