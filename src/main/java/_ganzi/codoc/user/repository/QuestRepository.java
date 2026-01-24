package _ganzi.codoc.user.repository;

import _ganzi.codoc.user.domain.Quest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestRepository extends JpaRepository<Quest, Integer> {}
