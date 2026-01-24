package _ganzi.codoc.user.repository;

import _ganzi.codoc.user.domain.User;
import _ganzi.codoc.user.domain.UserQuest;
import _ganzi.codoc.user.enums.QuestStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserQuestRepository extends JpaRepository<UserQuest, Long> {

    List<UserQuest> findAllByUser(User user);

    Optional<UserQuest> findByIdAndUser(Long id, User user);

    List<UserQuest> findAllByUserAndExpiresAtBefore(User user, Instant now);

    List<UserQuest> findAllByUserAndStatus(User user, QuestStatus status);

    List<UserQuest> findAllByUserAndStatusNot(User user, QuestStatus status);
}
