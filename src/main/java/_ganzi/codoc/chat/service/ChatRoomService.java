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
    private final ChatRoomParticipantRepository chatRoomParticipantRepository;
    private final PasswordEncoder passwordEncoder;
    private final ChatSystemMessagePublisher systemMessagePublisher;
    private final CursorPageFetcher cursorPageFetcher;
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

        chatRoom.incrementParticipantCount();

        ChatMessage systemMessage = systemMessagePublisher.publishJoin(chatRoom, user.getNickname());

        chatRoomParticipantRepository.save(
                ChatRoomParticipant.createOrRejoin(existing, userId, chatRoom, systemMessage.getId()));
    }

    @Transactional
    public void leaveAllChatRooms(Long userId, String nickname) {
        List<ChatRoomParticipant> participants =
                chatRoomParticipantRepository.findAllJoinedByUserId(userId);

        for (ChatRoomParticipant participant : participants) {
            participant.leave();

            ChatRoom chatRoom = participant.getChatRoom();
            chatRoom.decrementParticipantCount();

            systemMessagePublisher.publishLeave(chatRoom, nickname);
        }
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

        chatRoom.decrementParticipantCount();

        systemMessagePublisher.publishLeave(chatRoom, user.getNickname());
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
        boolean hasUnread =
                chatRoomParticipantRepository.existsJoinedParticipantWithUnreadMessages(userId);
        return new UserChatUnreadStatusResponse(hasUnread);
    }

    public UserChatRoomDetailResponse getUserChatRoom(Long userId, Long roomId) {
        ChatRoomParticipant participant =
                chatRoomParticipantRepository
                        .findJoinedParticipant(userId, roomId)
                        .orElseThrow(NoChatRoomParticipantException::new);
        return UserChatRoomDetailResponse.from(participant);
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
            BiFunction<ChatRoomCursorPayload, Pageable, List<ChatRoom>> queryFunction) {

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
            BiFunction<UserChatRoomCursorPayload, Pageable, List<ChatRoomParticipant>> queryFunction) {

        return cursorPageFetcher.fetch(
                cursor,
                limit,
                UserChatRoomCursorPayload.class,
                UserChatRoomCursorPayload::firstPage,
                queryFunction,
                participants -> {
                    Map<Long, Long> unreadCountByParticipantId = getUnreadCountByParticipantId(participants);

                    return participants.stream()
                            .map(
                                    participant ->
                                            UserChatRoomListItem.from(
                                                    participant,
                                                    unreadCountByParticipantId.getOrDefault(participant.getId(), 0L)))
                            .toList();
                },
                UserChatRoomCursorPayload::from);
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
