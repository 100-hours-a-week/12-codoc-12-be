package _ganzi.codoc.chat.service;

import _ganzi.codoc.chat.repository.ChatRoomParticipantRepository;
import _ganzi.codoc.chat.repository.ChatUnreadCountRepository;
import java.util.OptionalLong;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ChatUnreadCountService {

    private final ChatUnreadCountRepository chatUnreadCountRepository;
    private final ChatRoomParticipantRepository chatRoomParticipantRepository;

    public long getTotalUnreadCount(Long userId) {
        OptionalLong cached = chatUnreadCountRepository.findTotalUnreadCount(userId);
        if (cached.isPresent()) {
            return cached.getAsLong();
        }

        return syncTotalUnreadCount(userId);
    }

    public long increaseTotalUnreadCount(Long userId, long delta, boolean initializeIfMissing) {
        OptionalLong cached = chatUnreadCountRepository.findTotalUnreadCount(userId);
        if (cached.isPresent()) {
            return chatUnreadCountRepository.incrementTotalUnreadCount(userId, delta);
        }

        if (!initializeIfMissing) {
            return -1L;
        }

        return syncTotalUnreadCount(userId);
    }

    public long decreaseTotalUnreadCount(Long userId, long delta) {
        OptionalLong cached = chatUnreadCountRepository.findTotalUnreadCount(userId);
        if (cached.isPresent()) {
            return chatUnreadCountRepository.incrementTotalUnreadCount(userId, -delta);
        }

        return syncTotalUnreadCount(userId);
    }

    public long syncTotalUnreadCount(Long userId) {
        long totalUnreadCount = chatRoomParticipantRepository.countTotalUnreadMessagesByUserId(userId);
        chatUnreadCountRepository.saveTotalUnreadCount(userId, totalUnreadCount);
        return totalUnreadCount;
    }
}
