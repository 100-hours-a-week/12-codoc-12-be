package _ganzi.codoc.user.repository;

import _ganzi.codoc.user.domain.Avatar;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AvatarRepository extends JpaRepository<Avatar, Integer> {

    Optional<Avatar> findByIsDefaultTrue();
}
