package _ganzi.codoc.user.repository;

import _ganzi.codoc.user.domain.User;
import _ganzi.codoc.user.domain.UserQuest;
import _ganzi.codoc.user.enums.QuestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface UserQuestRepository extends JpaRepository<UserQuest, Long> {

    List<UserQuest> findAllByUser(User user);

    Optional<UserQuest> findByIdAndUser(Long id, User user);

    List<UserQuest> findAllByUserAndExpiresAtBefore(User user, Instant now);

    @Query(
            """
            select uq
            from UserQuest uq
            join fetch uq.quest q
            where uq.user = :user and uq.status = :status
            """)
    List<UserQuest> findAllByUserAndStatusFetchQuest(
            @Param("user") User user, @Param("status") QuestStatus status);

    List<UserQuest> findAllByUserAndStatusNot(User user, QuestStatus status);
}
