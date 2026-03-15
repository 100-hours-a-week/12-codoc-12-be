package _ganzi.codoc.chat.domain;

import _ganzi.codoc.global.domain.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "chat_room_latest_message")
@Entity
public class ChatRoomLatestMessage extends BaseTimeEntity {

    @Id
    @Column(name = "chat_room_id")
    private Long chatRoomId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chat_room_id")
    private ChatRoom chatRoom;

    @Column(name = "latest_text_message_id")
    private Long latestTextMessageId;

    @Column(name = "latest_message_preview", nullable = false, length = 50)
    private String latestMessagePreview;

    @Column(name = "latest_message_at", nullable = false)
    private Instant latestMessageAt;

    private ChatRoomLatestMessage(
            ChatRoom chatRoom,
            Long latestTextMessageId,
            String latestMessagePreview,
            Instant latestMessageAt) {
        this.chatRoom = chatRoom;
        this.latestTextMessageId = latestTextMessageId;
        this.latestMessagePreview = latestMessagePreview;
        this.latestMessageAt = latestMessageAt;
    }

    public static ChatRoomLatestMessage createInit(
            ChatRoom chatRoom, String latestMessagePreview, Instant latestMessageAt) {
        return new ChatRoomLatestMessage(chatRoom, null, latestMessagePreview, latestMessageAt);
    }
}
