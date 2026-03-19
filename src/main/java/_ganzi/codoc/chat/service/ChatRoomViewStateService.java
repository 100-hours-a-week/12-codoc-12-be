package _ganzi.codoc.chat.service;

import _ganzi.codoc.chat.dto.ChatRoomViewStateRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class ChatRoomViewStateService {

    private final ChatRoomSubscriptionService chatRoomSubscriptionService;
    private final SharedWebSocketStateService sharedWebSocketStateService;

    public void updateRoomViewState(
            Long userId, String sessionId, Long roomId, ChatRoomViewStateRequest request) {
        if (sessionId == null || !chatRoomSubscriptionService.canSubscribe(userId, roomId)) {
            log.warn(
                    "채팅방 view state 업데이트를 무시합니다. userId={}, sessionId={}, roomId={}",
                    userId,
                    sessionId,
                    roomId);
            return;
        }

        if (request.active()) {
            sharedWebSocketStateService.activateRoomView(sessionId, roomId);
            return;
        }

        sharedWebSocketStateService.deactivateRoomView(sessionId, roomId);
    }
}
