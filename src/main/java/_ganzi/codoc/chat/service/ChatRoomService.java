package _ganzi.codoc.chat.service;

import _ganzi.codoc.chat.config.ChatProperties;
import _ganzi.codoc.chat.domain.ChatMessage;
import _ganzi.codoc.chat.domain.ChatRoom;
import _ganzi.codoc.chat.domain.ChatRoomLatestMessage;
import _ganzi.codoc.chat.domain.ChatRoomParticipant;
import _ganzi.codoc.chat.dto.*;
import _ganzi.codoc.chat.dto.ChatRoomCreateRequest;
import _ganzi.codoc.chat.dto.ChatRoomCreateResponse;
import _ganzi.codoc.chat.event.ChatUnreadTotalSyncRequestedEvent;
import _ganzi.codoc.chat.exception.ChatRoomFullException;
import _ganzi.codoc.chat.exception.ChatRoomInvalidPasswordException;
import _ganzi.codoc.chat.exception.ChatRoomNotFoundException;
import _ganzi.codoc.chat.exception.NoChatRoomParticipantException;
import _ganzi.codoc.chat.repository.ChatMessageRepository;
import _ganzi.codoc.chat.repository.ChatRoomLatestMessageRepository;
import _ganzi.codoc.chat.repository.ChatRoomParticipantRepository;
import _ganzi.codoc.chat.repository.ChatRoomRepository;
import _ganzi.codoc.global.cursor.CursorPageFetcher;
import _ganzi.codoc.global.dto.CursorPagingResponse;
import _ganzi.codoc.user.domain.User;
import _ganzi.codoc.user.exception.UserNotFoundException;
import _ganzi.codoc.user.repository.UserRepository;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
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
    private final ChatRoomLatestMessageRepository chatRoomLatestMessageRepository;
    private final ChatRoomParticipantRepository chatRoomParticipantRepository;
    private final ChatUnreadCountService chatUnreadCountService;
    private final PasswordEncoder passwordEncoder;
    private final ChatSystemMessagePublisher systemMessagePublisher;
    private final CursorPageFetcher cursorPageFetcher;
    private final ChatProperties chatProperties;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public ChatRoomCreateResponse createChatRoom(Long userId, ChatRoomCreateRequest request) {
        ChatRoom chatRoom =
                chatRoomRepository.save(
                        ChatRoom.create(request.title(), passwordEncoder.encode(request.password())));

        ChatMessage initMessage =
                chatMessageRepository.save(ChatMessage.createInit(chatRoom, ROOM_CREATED_INIT_MESSAGE));

        chatRoomLatestMessageRepository.save(
                ChatRoomLatestMessage.createInit(
                        chatRoom,
                        ChatRoom.toListPreview(initMessage.getContent()),
                        initMessage.getCreatedAt()));

        chatRoomParticipantRepository.save(
                ChatRoomParticipant.create(userId, chatRoom, initMessage.getId()));

        return ChatRoomCreateResponse.from(chatRoom);
    }

    @Transactional
    public void joinChatRoom(Long userId, Long roomId, ChatRoomJoinRequest request) {
        ChatRoom chatRoom =
                chatRoomRepository
                        .findActiveByIdForUpdate(roomId)
                        .orElseThrow(ChatRoomNotFoundException::new);

        ChatRoomParticipant existing =
                chatRoomParticipantRepository.findByUserIdAndRoomId(userId, roomId).orElse(null);
        ChatRoomParticipant.validateJoinable(existing);

        if (chatRoom.hasPassword()) {
            String password = request != null ? request.password() : null;
            if (password == null || !passwordEncoder.matches(password, chatRoom.getPassword())) {
                throw new ChatRoomInvalidPasswordException();
            }
        }

        long participantCount = chatRoomParticipantRepository.countJoinedParticipantsByRoomId(roomId);
        if (participantCount >= chatProperties.maxParticipants()) {
            throw new ChatRoomFullException();
        }

        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);

        ChatMessage systemMessage =
                systemMessagePublisher.publishJoin(
                        chatRoom, user.getNickname(), Math.toIntExact(participantCount + 1));

        chatRoomParticipantRepository.save(
                ChatRoomParticipant.createOrRejoin(existing, userId, chatRoom, systemMessage.getId()));
    }

    @Transactional
    public void leaveAllChatRooms(Long userId, String nickname) {
        List<ChatRoomParticipant> participants =
                chatRoomParticipantRepository.findAllJoinedByUserId(userId);

        for (ChatRoomParticipant participant : participants) {
            ChatRoom chatRoom = participant.getChatRoom();
            chatRoomRepository
                    .findActiveByIdForUpdate(chatRoom.getId())
                    .orElseThrow(ChatRoomNotFoundException::new);
            long participantCount =
                    chatRoomParticipantRepository.countJoinedParticipantsByRoomId(chatRoom.getId());
            participant.leave();
            int afterLeaveCount = Math.max(0, Math.toIntExact(participantCount - 1));
            if (afterLeaveCount == 0) {
                chatRoomRepository.markDeletedIfEmpty(chatRoom.getId(), Instant.now());
            }

            systemMessagePublisher.publishLeave(chatRoom, nickname, afterLeaveCount, afterLeaveCount > 0);
        }

        applicationEventPublisher.publishEvent(new ChatUnreadTotalSyncRequestedEvent(userId));
    }

    @Transactional
    public void leaveChatRoom(Long userId, Long roomId) {
        ChatRoomParticipant participant =
                chatRoomParticipantRepository
                        .findJoinedParticipant(userId, roomId)
                        .orElseThrow(NoChatRoomParticipantException::new);

        ChatRoom chatRoom = participant.getChatRoom();
        chatRoomRepository
                .findActiveByIdForUpdate(chatRoom.getId())
                .orElseThrow(ChatRoomNotFoundException::new);
        long participantCount =
                chatRoomParticipantRepository.countJoinedParticipantsByRoomId(chatRoom.getId());
        participant.leave();
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        int afterLeaveCount = Math.max(0, Math.toIntExact(participantCount - 1));
        if (afterLeaveCount == 0) {
            chatRoomRepository.markDeletedIfEmpty(chatRoom.getId(), Instant.now());
        }

        systemMessagePublisher.publishLeave(
                chatRoom, user.getNickname(), afterLeaveCount, afterLeaveCount > 0);

        applicationEventPublisher.publishEvent(new ChatUnreadTotalSyncRequestedEvent(userId));
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

    public UserChatUnreadStatusResponse getUserChatUnreadStatus(Long userId) {
        return UserChatUnreadStatusResponse.from(chatUnreadCountService.getTotalUnreadCount(userId));
    }

    public UserChatRoomDetailResponse getUserChatRoom(Long userId, Long roomId) {
        ChatRoomParticipant participant =
                chatRoomParticipantRepository
                        .findJoinedParticipant(userId, roomId)
                        .orElseThrow(NoChatRoomParticipantException::new);
        int participantCount =
                Math.toIntExact(chatRoomParticipantRepository.countJoinedParticipantsByRoomId(roomId));
        return UserChatRoomDetailResponse.from(participant, participantCount);
    }

    public CursorPagingResponse<ChatRoomListItem, String> getAllChatRooms(
            String cursor, Integer limit) {

        return fetchAllChatRooms(
                cursor,
                limit,
                (cursorPayload, pageable) ->
                        chatRoomRepository.findLatestChatRooms(
                                cursorPayload.orderedAt(), cursorPayload.roomId(), pageable));
    }

    public CursorPagingResponse<ChatRoomListItem, String> searchAllChatRooms(
            String keyword, String cursor, Integer limit) {

        return fetchAllChatRooms(
                cursor,
                limit,
                (cursorPayload, pageable) ->
                        chatRoomRepository.searchChatRoomsByKeyword(
                                keyword, cursorPayload.orderedAt(), cursorPayload.roomId(), pageable));
    }

    private CursorPagingResponse<ChatRoomListItem, String> fetchAllChatRooms(
            String cursor,
            Integer limit,
            BiFunction<ChatRoomCursorPayload, Pageable, List<ChatRoomListQueryResult>> queryFunction) {

        return cursorPageFetcher.fetch(
                cursor,
                limit,
                ChatRoomCursorPayload.class,
                ChatRoomCursorPayload::firstPage,
                queryFunction,
                fetchedRooms ->
                        fetchedRooms.stream()
                                .map(chatRoom -> ChatRoomListItem.from(chatRoom, chatProperties.maxParticipants()))
                                .toList(),
                ChatRoomCursorPayload::from);
    }

    private CursorPagingResponse<UserChatRoomListItem, String> fetchUserChatRooms(
            String cursor,
            Integer limit,
            BiFunction<UserChatRoomCursorPayload, Pageable, List<UserChatRoomListRow>> queryFunction) {

        return cursorPageFetcher.fetch(
                cursor,
                limit,
                UserChatRoomCursorPayload.class,
                UserChatRoomCursorPayload::firstPage,
                queryFunction,
                this::buildUserChatRoomItems,
                UserChatRoomCursorPayload::from);
    }

    private List<UserChatRoomListItem> buildUserChatRoomItems(List<UserChatRoomListRow> rows) {
        if (rows.isEmpty()) {
            return List.of();
        }

        List<Long> pagedParticipantIds =
                rows.stream().map(UserChatRoomListRow::getParticipantId).toList();
        List<Long> pagedRoomIds = rows.stream().map(UserChatRoomListRow::getRoomId).distinct().toList();
        Map<Long, Long> unreadCountByParticipantId = getUnreadCountByParticipantId(pagedParticipantIds);
        Map<Long, Long> participantCountByRoomId = getParticipantCountByRoomId(pagedRoomIds);

        return rows.stream()
                .map(
                        row ->
                                new UserChatRoomListItem(
                                        row.getRoomId(),
                                        row.getTitle(),
                                        Math.toIntExact(participantCountByRoomId.getOrDefault(row.getRoomId(), 0L)),
                                        row.getLastMessagePreview(),
                                        row.getLastMessageAt(),
                                        unreadCountByParticipantId.getOrDefault(row.getParticipantId(), 0L)))
                .toList();
    }

    private Map<Long, Long> getUnreadCountByParticipantId(List<Long> participantIds) {
        if (participantIds.isEmpty()) {
            return Map.of();
        }

        return chatRoomParticipantRepository
                .countUnreadMessagesByParticipantIds(participantIds)
                .stream()
                .collect(
                        Collectors.toMap(
                                ParticipantUnreadMessageCountRow::getParticipantId,
                                ParticipantUnreadMessageCountRow::getUnreadCount));
    }

    private Map<Long, Long> getParticipantCountByRoomId(List<Long> roomIds) {
        if (roomIds.isEmpty()) {
            return Map.of();
        }

        return chatRoomParticipantRepository.countJoinedParticipantsByRoomIds(roomIds).stream()
                .collect(
                        Collectors.toMap(
                                RoomParticipantCount::roomId, RoomParticipantCount::participantsCount));
    }
}
