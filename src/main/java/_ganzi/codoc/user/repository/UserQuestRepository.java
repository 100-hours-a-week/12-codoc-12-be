package _ganzi.codoc.user.repository;

import _ganzi.codoc.user.domain.User;
import _ganzi.codoc.user.domain.UserQuest;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserQuestRepository extends JpaRepository<UserQuest, Long> {

    Optional<UserQuest> findByIdAndUser(Long id, User user);

    @Query(
            "select uq from UserQuest uq join fetch uq.quest q "
                    + "where uq.user = :user and uq.isExpired = false")
    List<UserQuest> findAllByUserAndIsExpiredFalseFetchQuest(@Param("user") User user);
}
