package _ganzi.codoc.chat.service;

import _ganzi.codoc.chat.domain.ChatMessage;
import _ganzi.codoc.chat.domain.ChatRoom;
import _ganzi.codoc.chat.domain.ChatRoomParticipant;
import _ganzi.codoc.chat.dto.*;
import _ganzi.codoc.chat.event.ChatMessageCommittedEvent;
import _ganzi.codoc.chat.event.ChatUnreadTotalAdjustedEvent;
import _ganzi.codoc.chat.exception.NoChatRoomParticipantException;
import _ganzi.codoc.chat.repository.ChatMessageRepository;
import _ganzi.codoc.chat.repository.ChatRoomLatestMessageRepository;
import _ganzi.codoc.chat.repository.ChatRoomParticipantRepository;
import _ganzi.codoc.global.cursor.CursorPageFetcher;
import _ganzi.codoc.global.dto.CursorPagingResponse;
import _ganzi.codoc.user.domain.User;
import _ganzi.codoc.user.exception.UserNotFoundException;
import _ganzi.codoc.user.repository.UserRepository;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class ChatMessageService {

    private final UserRepository userRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomLatestMessageRepository chatRoomLatestMessageRepository;
    private final ChatRoomParticipantRepository chatRoomParticipantRepository;
    private final SharedWebSocketStateService sharedWebSocketStateService;
    private final CursorPageFetcher cursorPageFetcher;
    private final ApplicationEventPublisher applicationEventPublisher;

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
                messages -> {
                    int participantCount =
                            Math.toIntExact(
                                    chatRoomParticipantRepository.countJoinedParticipantsByRoomId(roomId));
                    return messages.stream()
                            .map(
                                    message ->
                                            new ChatMessageListItem(
                                                    message.messageId(),
                                                    message.senderId(),
                                                    message.senderNickname(),
                                                    message.senderAvatarImageUrl(),
                                                    message.type(),
                                                    message.content(),
                                                    participantCount,
                                                    message.createdAt()))
                            .toList();
                },
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

        Instant lastMessageAt = message.getCreatedAt() != null ? message.getCreatedAt() : Instant.now();
        String lastMessagePreview = ChatRoom.toListPreview(request.content());
        chatRoomLatestMessageRepository.updateLatestTextMessageIfNewer(
                roomId, message.getId(), lastMessagePreview, lastMessageAt);

        Set<Long> onlineUserIds = sharedWebSocketStateService.getActiveSubscriberUserIds(roomId);
        int participantCount =
                Math.toIntExact(chatRoomParticipantRepository.countJoinedParticipantsByRoomId(roomId));

        ChatMessageBroadcast roomMessage =
                ChatMessageBroadcast.from(
                        message, sender.getNickname(), sender.getAvatar().getImageUrl(), participantCount);

        List<Long> allParticipantUserIds =
                chatRoomParticipantRepository.findJoinedUserIdsByRoomId(roomId);

        ChatRoomUpdateBroadcast roomUpdate =
                new ChatRoomUpdateBroadcast(roomId, lastMessagePreview, lastMessageAt);

        applicationEventPublisher.publishEvent(
                new ChatMessageCommittedEvent(
                        roomId,
                        roomMessage,
                        roomUpdate,
                        onlineUserIds,
                        allParticipantUserIds,
                        sender.getNickname()));
    }

    @Transactional
    public void ackReadMessage(Long userId, Long roomId, ChatMessageReadAckRequest request) {
        ChatRoomParticipant participant =
                chatRoomParticipantRepository
                        .findJoinedParticipant(userId, roomId)
                        .orElseThrow(NoChatRoomParticipantException::new);

        if (!chatMessageRepository.existsMessageInRoom(roomId, request.lastReadMessageId())) {
            return;
        }

        long previousLastReadMessageId = participant.getLastReadMessageId();
        if (request.lastReadMessageId() <= previousLastReadMessageId) {
            return;
        }

        long newlyReadTextCount =
                chatMessageRepository.countTextMessagesInRange(
                        roomId, previousLastReadMessageId, request.lastReadMessageId());
        chatRoomParticipantRepository.ackLastReadMessageId(roomId, userId, request.lastReadMessageId());

        if (newlyReadTextCount > 0) {
            applicationEventPublisher.publishEvent(
                    new ChatUnreadTotalAdjustedEvent(userId, newlyReadTextCount));
        }
    }
}
