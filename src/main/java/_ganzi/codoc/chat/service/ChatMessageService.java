package _ganzi.codoc.chat.service;

import _ganzi.codoc.chat.config.ChatRoomSubscriptionRegistry;
import _ganzi.codoc.chat.config.WebSocketSessionRegistry;
import _ganzi.codoc.chat.domain.ChatMessage;
import _ganzi.codoc.chat.domain.ChatRoom;
import _ganzi.codoc.chat.domain.ChatRoomParticipant;
import _ganzi.codoc.chat.dto.*;
import _ganzi.codoc.chat.exception.NoChatRoomParticipantException;
import _ganzi.codoc.chat.repository.ChatMessageRepository;
import _ganzi.codoc.chat.repository.ChatRoomParticipantRepository;
import _ganzi.codoc.global.cursor.CursorPageFetcher;
import _ganzi.codoc.global.dto.CursorPagingResponse;
import _ganzi.codoc.notification.dto.NotificationMessageItem;
import _ganzi.codoc.notification.enums.NotificationType;
import _ganzi.codoc.notification.service.NotificationDispatchService;
import _ganzi.codoc.user.domain.User;
import _ganzi.codoc.user.exception.UserNotFoundException;
import _ganzi.codoc.user.repository.UserRepository;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class ChatMessageService {

    private final UserRepository userRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomParticipantRepository chatRoomParticipantRepository;
    private final ChatRoomSubscriptionRegistry chatRoomSubscriptionRegistry;
    private final WebSocketSessionRegistry webSocketSessionRegistry;
    private final CursorPageFetcher cursorPageFetcher;
    private final ChatBroadcaster chatBroadcaster;
    private final NotificationDispatchService notificationDispatchService;

    public CursorPagingResponse<ChatMessageListItem, String> getMessages(
            Long userId, Long roomId, String cursor, Integer limit) {

        chatRoomParticipantRepository
                .findJoinedParticipant(userId, roomId)
                .orElseThrow(NoChatRoomParticipantException::new);

        return cursorPageFetcher.fetch(
                cursor,
                limit,
                ChatMessageCursorPayload.class,
                ChatMessageCursorPayload::firstPage,
                (cursorPayload, pageable) ->
                        chatMessageRepository.findVisibleMessages(roomId, cursorPayload.messageId(), pageable),
                Function.identity(),
                ChatMessageCursorPayload::from);
    }

    @Transactional
    public void sendMessage(Long userId, Long roomId, ChatMessageSendRequest request) {
        ChatRoomParticipant participant =
                chatRoomParticipantRepository
                        .findJoinedParticipant(userId, roomId)
                        .orElseThrow(NoChatRoomParticipantException::new);

        ChatRoom chatRoom = participant.getChatRoom();
        User sender = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        ChatMessage message = ChatMessage.createText(userId, chatRoom, request.content());
        chatMessageRepository.save(message);
        chatRoom.applyLastMessage(message);

        Set<Long> onlineUserIds = chatRoomSubscriptionRegistry.getSubscriberUserIds(roomId);

        if (!onlineUserIds.isEmpty()) {
            chatRoomParticipantRepository.updateLastReadMessageId(roomId, onlineUserIds, message.getId());
        }

        chatBroadcaster.broadcastMessage(
                roomId,
                ChatMessageBroadcast.from(
                        message,
                        sender.getNickname(),
                        sender.getAvatar().getImageUrl(),
                        chatRoom.getParticipantCount()));

        List<Long> allParticipantUserIds =
                chatRoomParticipantRepository.findJoinedUserIdsByRoomId(roomId);

        ChatRoomUpdateBroadcast roomUpdate =
                new ChatRoomUpdateBroadcast(
                        roomId, chatRoom.getLastMessagePreview(), chatRoom.getLastMessageAt());

        for (Long participantUserId : allParticipantUserIds) {
            if (onlineUserIds.contains(participantUserId)) {
                continue;
            }

            if (webSocketSessionRegistry.isConnected(participantUserId)) {
                chatBroadcaster.broadcastRoomUpdate(participantUserId, roomUpdate);
                continue;
            }

            notificationDispatchService.dispatchAfterCommit(
                    participantUserId,
                    new NotificationMessageItem(
                            NotificationType.CHAT,
                            sender.getNickname(),
                            chatRoom.getLastMessagePreview(),
                            Map.of("roomId", String.valueOf(roomId))));
        }
    }
}
