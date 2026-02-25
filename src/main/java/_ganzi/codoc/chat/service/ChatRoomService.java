package _ganzi.codoc.chat.service;

import _ganzi.codoc.chat.domain.ChatMessage;
import _ganzi.codoc.chat.domain.ChatRoom;
import _ganzi.codoc.chat.domain.ChatRoomParticipant;
import _ganzi.codoc.chat.dto.ChatRoomCreateRequest;
import _ganzi.codoc.chat.dto.ChatRoomCreateResponse;
import _ganzi.codoc.chat.repository.ChatMessageRepository;
import _ganzi.codoc.chat.repository.ChatRoomParticipantRepository;
import _ganzi.codoc.chat.repository.ChatRoomRepository;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class ChatRoomService {

    private static final String ROOM_CREATED_INIT_MESSAGE = "새로운 채팅방이 생성되었습니다";

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomParticipantRepository chatRoomParticipantRepository;
    private final PasswordEncoder passwordEncoder;

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
}
