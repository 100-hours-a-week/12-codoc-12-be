package _ganzi.codoc.user.repository;

import _ganzi.codoc.user.domain.BadWord;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BadWordRepository extends JpaRepository<BadWord, Long> {
    List<BadWord> findAll();
}
