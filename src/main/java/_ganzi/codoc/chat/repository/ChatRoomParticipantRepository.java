package _ganzi.codoc.chat.repository;

import _ganzi.codoc.chat.domain.ChatRoomParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRoomParticipantRepository extends JpaRepository<ChatRoomParticipant, Long> {}
