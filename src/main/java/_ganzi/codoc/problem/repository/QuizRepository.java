package _ganzi.codoc.problem.repository;

import _ganzi.codoc.problem.domain.Quiz;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizRepository extends JpaRepository<Quiz, Long> {

    List<Quiz> findByProblemIdOrderBySequenceAsc(Long problemId);

    int countByProblemId(Long problemId);
}
