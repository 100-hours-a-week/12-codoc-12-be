package _ganzi.codoc.chat.service;

import _ganzi.codoc.chat.domain.ChatMessage;
import _ganzi.codoc.chat.domain.ChatRoom;
import _ganzi.codoc.chat.domain.ChatRoomParticipant;
import _ganzi.codoc.chat.dto.*;
import _ganzi.codoc.chat.exception.NoChatRoomParticipantException;
import _ganzi.codoc.chat.repository.ChatMessageRepository;
import _ganzi.codoc.chat.repository.ChatRoomParticipantRepository;
import _ganzi.codoc.global.cursor.CursorCodec;
import _ganzi.codoc.global.cursor.CursorPayloadConverter;
import _ganzi.codoc.global.dto.CursorPagingResponse;
import _ganzi.codoc.global.util.CursorPagingUtils;
import _ganzi.codoc.global.util.PageLimitResolver;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomParticipantRepository chatRoomParticipantRepository;
    private final CursorCodec cursorCodec;
    private final SimpMessagingTemplate messagingTemplate;

    public CursorPagingResponse<ChatMessageListItem, String> getMessages(
            Long userId, Long roomId, String cursor, Integer limit) {

        ChatRoomParticipant participant =
                chatRoomParticipantRepository
                        .findJoinedParticipant(userId, roomId)
                        .orElseThrow(NoChatRoomParticipantException::new);

        int resolvedLimit = PageLimitResolver.resolve(limit);
        ChatMessageCursorPayload cursorPayload =
                CursorPayloadConverter.decodeAndValidate(
                        cursorCodec,
                        cursor,
                        ChatMessageCursorPayload.class,
                        ChatMessageCursorPayload::firstPage);

        Pageable pageable = CursorPagingUtils.createPageable(resolvedLimit);
        List<ChatMessage> messages =
                chatMessageRepository.findVisibleMessages(
                        roomId, participant.getJoinedMessageId(), cursorPayload.messageId(), pageable);

        List<ChatMessageListItem> items = messages.stream().map(ChatMessageListItem::from).toList();

        return CursorPagingUtils.apply(
                items, resolvedLimit, item -> cursorCodec.encode(ChatMessageCursorPayload.from(item)));
    }

    @Transactional
    public void sendMessage(Long userId, Long roomId, ChatMessageSendRequest request) {
        ChatRoomParticipant participant =
                chatRoomParticipantRepository
                        .findJoinedParticipant(userId, roomId)
                        .orElseThrow(NoChatRoomParticipantException::new);

        ChatRoom chatRoom = participant.getChatRoom();
        ChatMessage message = ChatMessage.createText(userId, chatRoom, request.content());
        chatMessageRepository.save(message);
        chatRoom.applyLastMessage(message);

        messagingTemplate.convertAndSend(
                "/sub/chat/rooms/" + roomId, ChatMessageBroadcast.from(message));
    }
}
