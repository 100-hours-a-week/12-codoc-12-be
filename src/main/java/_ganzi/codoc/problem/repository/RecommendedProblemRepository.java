package _ganzi.codoc.problem.repository;

import _ganzi.codoc.problem.domain.RecommendedProblem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecommendedProblemRepository extends JpaRepository<RecommendedProblem, Long> {}
