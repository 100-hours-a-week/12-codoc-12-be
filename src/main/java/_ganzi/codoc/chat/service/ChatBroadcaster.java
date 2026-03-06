package _ganzi.codoc.chat.service;

import _ganzi.codoc.chat.dto.ChatMessageBroadcast;
import _ganzi.codoc.chat.dto.ChatRoomUpdateBroadcast;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ChatBroadcaster {

    private final SimpMessagingTemplate messagingTemplate;

    public void broadcastMessage(Long roomId, ChatMessageBroadcast broadcast) {
        messagingTemplate.convertAndSend("/sub/chat/rooms/" + roomId, broadcast);
    }

    public void broadcastRoomUpdate(Long userId, ChatRoomUpdateBroadcast broadcast) {
        messagingTemplate.convertAndSend("/sub/users/" + userId + "/chat-rooms", broadcast);
    }
}
