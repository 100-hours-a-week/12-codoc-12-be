package _ganzi.codoc.chat.domain;

import _ganzi.codoc.chat.enums.ChatMessageType;
import _ganzi.codoc.global.domain.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "chat_message")
@Entity
public class ChatMessage extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sender_id", nullable = true)
    private Long senderId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private ChatMessageType type;

    @Column(name = "content", nullable = false, length = 500)
    private String content;

    private ChatMessage(Long senderId, ChatRoom chatRoom, ChatMessageType type, String content) {
        validateSenderIdPolicy(senderId, type);
        this.senderId = senderId;
        this.chatRoom = chatRoom;
        this.type = type;
        this.content = content;
    }

    public static ChatMessage createText(Long senderId, ChatRoom chatRoom, String content) {
        return new ChatMessage(senderId, chatRoom, ChatMessageType.TEXT, content);
    }

    public static ChatMessage createInit(ChatRoom chatRoom, String content) {
        return new ChatMessage(null, chatRoom, ChatMessageType.INIT, content);
    }

    public static ChatMessage createSystem(ChatRoom chatRoom, String content) {
        return new ChatMessage(null, chatRoom, ChatMessageType.SYSTEM, content);
    }

    private static void validateSenderIdPolicy(Long senderId, ChatMessageType type) {
        if (type.isSenderIdRequired() && senderId == null) {
            throw new IllegalArgumentException(type + " message requires senderId");
        }

        if (!type.isSenderIdRequired() && senderId != null) {
            throw new IllegalArgumentException(type + " message must not have senderId");
        }
    }
}
