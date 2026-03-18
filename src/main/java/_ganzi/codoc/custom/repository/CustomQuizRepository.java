package _ganzi.codoc.custom.repository;

import _ganzi.codoc.custom.domain.CustomQuiz;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CustomQuizRepository extends JpaRepository<CustomQuiz, Long> {

    @Query(
            """
            select q
            from CustomQuiz q
            where q.customProblem.id = :customProblemId
            order by q.sequence asc
            """)
    List<CustomQuiz> findAllByCustomProblemId(@Param("customProblemId") Long customProblemId);
}
