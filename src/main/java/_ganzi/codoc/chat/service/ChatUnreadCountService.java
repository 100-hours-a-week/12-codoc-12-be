package _ganzi.codoc.chat.service;

import _ganzi.codoc.chat.repository.ChatRoomParticipantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ChatUnreadCountService {

    private final ChatRoomParticipantRepository chatRoomParticipantRepository;

    public long getTotalUnreadCount(Long userId) {
        return chatRoomParticipantRepository.countTotalUnreadMessagesByUserId(userId);
    }
}
