package _ganzi.codoc.chat.service;

import _ganzi.codoc.chat.config.ChatProperties;
import _ganzi.codoc.chat.domain.ChatMessage;
import _ganzi.codoc.chat.domain.ChatRoom;
import _ganzi.codoc.chat.domain.ChatRoomParticipant;
import _ganzi.codoc.chat.dto.*;
import _ganzi.codoc.chat.dto.ChatRoomCreateRequest;
import _ganzi.codoc.chat.dto.ChatRoomCreateResponse;
import _ganzi.codoc.chat.exception.ChatRoomFullException;
import _ganzi.codoc.chat.exception.ChatRoomInvalidPasswordException;
import _ganzi.codoc.chat.exception.ChatRoomNotFoundException;
import _ganzi.codoc.chat.exception.NoChatRoomParticipantException;
import _ganzi.codoc.chat.repository.ChatMessageRepository;
import _ganzi.codoc.chat.repository.ChatRoomParticipantRepository;
import _ganzi.codoc.chat.repository.ChatRoomRepository;
import _ganzi.codoc.global.cursor.CursorCodec;
import _ganzi.codoc.global.cursor.CursorPayloadConverter;
import _ganzi.codoc.global.dto.CursorPagingResponse;
import _ganzi.codoc.global.util.CursorPagingUtils;
import _ganzi.codoc.global.util.PageLimitResolver;
import _ganzi.codoc.user.domain.User;
import _ganzi.codoc.user.exception.UserNotFoundException;
import _ganzi.codoc.user.repository.UserRepository;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class ChatRoomService {

    private static final String ROOM_CREATED_INIT_MESSAGE = "새로운 채팅방이 생성되었습니다";

    private final UserRepository userRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomParticipantRepository chatRoomParticipantRepository;
    private final PasswordEncoder passwordEncoder;
    private final SimpMessagingTemplate messagingTemplate;
    private final CursorCodec cursorCodec;
    private final ChatProperties chatProperties;

    @Transactional
    public ChatRoomCreateResponse createChatRoom(Long userId, ChatRoomCreateRequest request) {
        ChatRoom chatRoom =
                chatRoomRepository.save(
                        ChatRoom.create(
                                request.title(),
                                passwordEncoder.encode(request.password()),
                                ROOM_CREATED_INIT_MESSAGE,
                                Instant.now()));

        ChatMessage initMessage =
                chatMessageRepository.save(ChatMessage.createInit(chatRoom, ROOM_CREATED_INIT_MESSAGE));

        chatRoom.applyLastMessage(initMessage);

        chatRoomParticipantRepository.save(
                ChatRoomParticipant.create(userId, chatRoom, initMessage.getId()));

        return ChatRoomCreateResponse.from(chatRoom);
    }

    @Transactional
    public void joinChatRoom(Long userId, Long roomId, ChatRoomJoinRequest request) {
        ChatRoom chatRoom =
                chatRoomRepository.findActiveById(roomId).orElseThrow(ChatRoomNotFoundException::new);

        ChatRoomParticipant existing =
                chatRoomParticipantRepository.findByUserIdAndRoomId(userId, roomId).orElse(null);
        ChatRoomParticipant.validateJoinable(existing);

        if (chatRoom.hasPassword()) {
            String password = request != null ? request.password() : null;
            if (password == null || !passwordEncoder.matches(password, chatRoom.getPassword())) {
                throw new ChatRoomInvalidPasswordException();
            }
        }

        if (chatRoom.getParticipantCount() >= chatProperties.maxParticipants()) {
            throw new ChatRoomFullException();
        }

        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);

        ChatMessage systemMessage =
                chatMessageRepository.save(
                        ChatMessage.createSystem(chatRoom, user.getNickname() + "님이 입장했습니다."));

        chatRoomParticipantRepository.save(
                ChatRoomParticipant.createOrRejoin(existing, userId, chatRoom, systemMessage.getId()));

        chatRoom.incrementParticipantCount();

        messagingTemplate.convertAndSend(
                "/sub/chat/rooms/" + roomId, ChatMessageBroadcast.from(systemMessage, null));
    }

    @Transactional
    public void leaveChatRoom(Long userId, Long roomId) {
        ChatRoomParticipant participant =
                chatRoomParticipantRepository
                        .findJoinedParticipant(userId, roomId)
                        .orElseThrow(NoChatRoomParticipantException::new);

        participant.leave();

        ChatRoom chatRoom = participant.getChatRoom();
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);

        ChatMessage systemMessage =
                chatMessageRepository.save(
                        ChatMessage.createSystem(chatRoom, user.getNickname() + "님이 퇴장했습니다."));

        chatRoom.decrementParticipantCount();

        messagingTemplate.convertAndSend(
                "/sub/chat/rooms/" + roomId, ChatMessageBroadcast.from(systemMessage, null));
    }

    public CursorPagingResponse<UserChatRoomListItem, String> getUserChatRooms(
            Long userId, String cursor, Integer limit) {

        return fetchUserChatRooms(
                cursor,
                limit,
                (cursorPayload, pageable) ->
                        chatRoomParticipantRepository.findLatestJoinedChatRoomsByUserId(
                                userId, cursorPayload.orderedAt(), cursorPayload.roomId(), pageable));
    }

    public CursorPagingResponse<UserChatRoomListItem, String> searchUserChatRooms(
            Long userId, String keyword, String cursor, Integer limit) {

        return fetchUserChatRooms(
                cursor,
                limit,
                (cursorPayload, pageable) ->
                        chatRoomParticipantRepository.searchJoinedChatRoomsByKeyword(
                                userId, keyword, cursorPayload.orderedAt(), cursorPayload.roomId(), pageable));
    }

    public CursorPagingResponse<ChatRoomListItem, String> searchAllChatRooms(
            String keyword, String cursor, Integer limit) {

        int resolvedLimit = PageLimitResolver.resolve(limit);
        ChatRoomSearchCursorPayload cursorPayload =
                CursorPayloadConverter.decodeAndValidate(
                        cursorCodec,
                        cursor,
                        ChatRoomSearchCursorPayload.class,
                        ChatRoomSearchCursorPayload::firstPage);

        Pageable pageable = CursorPagingUtils.createPageable(resolvedLimit);
        List<ChatRoom> fetchedRooms =
                chatRoomRepository.searchChatRoomsByKeyword(
                        keyword, cursorPayload.orderedAt(), cursorPayload.roomId(), pageable);

        List<ChatRoomListItem> rooms =
                fetchedRooms.stream()
                        .map(chatRoom -> ChatRoomListItem.from(chatRoom, chatProperties.maxParticipants()))
                        .toList();

        return CursorPagingUtils.apply(
                rooms, resolvedLimit, item -> cursorCodec.encode(ChatRoomSearchCursorPayload.from(item)));
    }

    private CursorPagingResponse<UserChatRoomListItem, String> fetchUserChatRooms(
            String cursor,
            Integer limit,
            BiFunction<UserChatRoomCursorPayload, Pageable, List<ChatRoomParticipant>> queryFunction) {

        int resolvedLimit = PageLimitResolver.resolve(limit);
        UserChatRoomCursorPayload cursorPayload =
                CursorPayloadConverter.decodeAndValidate(
                        cursorCodec,
                        cursor,
                        UserChatRoomCursorPayload.class,
                        UserChatRoomCursorPayload::firstPage);

        Pageable pageable = CursorPagingUtils.createPageable(resolvedLimit);
        List<ChatRoomParticipant> participants = queryFunction.apply(cursorPayload, pageable);

        Map<Long, Long> unreadCountByParticipantId = getUnreadCountByParticipantId(participants);

        List<UserChatRoomListItem> rooms =
                participants.stream()
                        .map(
                                participant ->
                                        UserChatRoomListItem.from(
                                                participant,
                                                unreadCountByParticipantId.getOrDefault(participant.getId(), 0L)))
                        .toList();

        return CursorPagingUtils.apply(
                rooms, resolvedLimit, item -> cursorCodec.encode(UserChatRoomCursorPayload.from(item)));
    }

    private Map<Long, Long> getUnreadCountByParticipantId(List<ChatRoomParticipant> participants) {
        if (participants.isEmpty()) {
            return Map.of();
        }

        List<Long> participantIds = participants.stream().map(ChatRoomParticipant::getId).toList();

        return chatRoomParticipantRepository
                .countUnreadMessagesByParticipantIds(participantIds)
                .stream()
                .collect(
                        Collectors.toMap(
                                ParticipantUnreadMessageCount::participantId,
                                ParticipantUnreadMessageCount::unreadCount));
    }
}
