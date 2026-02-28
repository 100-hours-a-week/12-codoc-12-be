package _ganzi.codoc.chat.service;

import _ganzi.codoc.chat.repository.ChatRoomParticipantRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class ChatRoomSubscriptionService {

    private final ChatRoomParticipantRepository participantRepository;

    @Transactional
    public boolean markLastReadOnSubscribe(Long userId, Long roomId) {
        Long lastMessageId = participantRepository.findLastMessageIdByJoinedParticipant(userId, roomId);
        if (lastMessageId == null) {
            return false;
        }

        participantRepository.updateLastReadMessageId(roomId, List.of(userId), lastMessageId);
        return true;
    }
}
