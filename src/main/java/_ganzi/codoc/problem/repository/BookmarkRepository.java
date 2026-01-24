package _ganzi.codoc.problem.repository;

import _ganzi.codoc.problem.domain.Bookmark;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    boolean existsByUserIdAndProblemId(Long userId, Long problemId);

    void deleteByUserIdAndProblemId(Long userId, Long problemId);
}
