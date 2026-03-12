package _ganzi.codoc.chat.service;

import _ganzi.codoc.chat.domain.ChatMessage;
import _ganzi.codoc.chat.domain.ChatRoom;
import _ganzi.codoc.chat.dto.ChatMessageBroadcast;
import _ganzi.codoc.chat.event.ChatSystemMessageCommittedEvent;
import _ganzi.codoc.chat.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ChatSystemMessagePublisher {

    private final ChatMessageRepository chatMessageRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    public ChatMessage publishJoin(ChatRoom chatRoom, String nickname) {
        return publish(chatRoom, nickname + "님이 입장했습니다.");
    }

    public void publishLeave(ChatRoom chatRoom, String nickname) {
        publish(chatRoom, nickname + "님이 퇴장했습니다.");
    }

    private ChatMessage publish(ChatRoom chatRoom, String content) {
        ChatMessage systemMessage =
                chatMessageRepository.save(ChatMessage.createSystem(chatRoom, content));

        if (!chatRoom.isDeleted()) {
            applicationEventPublisher.publishEvent(
                    new ChatSystemMessageCommittedEvent(
                            chatRoom.getId(),
                            ChatMessageBroadcast.from(
                                    systemMessage, null, null, chatRoom.getParticipantCount())));
        }

        return systemMessage;
    }
}
