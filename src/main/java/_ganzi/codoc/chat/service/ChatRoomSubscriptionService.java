package _ganzi.codoc.chat.service;

import _ganzi.codoc.chat.repository.ChatRoomParticipantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class ChatRoomSubscriptionService {

    private final ChatRoomParticipantRepository participantRepository;

    public boolean canSubscribe(Long userId, Long roomId) {
        return participantRepository.existsJoinedParticipant(userId, roomId);
    }
}
