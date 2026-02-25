package _ganzi.codoc.chat.domain;

import _ganzi.codoc.global.domain.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "chat_room_participant")
@Entity
public class ChatRoomParticipant extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @Column(name = "joined_message_id", nullable = false)
    private long joinedMessageId;

    @Column(name = "last_read_message_id", nullable = false)
    private long lastReadMessageId;

    @Column(name = "is_joined", nullable = false)
    private boolean isJoined;

    @Column(name = "joined_at", nullable = false)
    private Instant joinedAt;

    @Column(name = "leaved_at")
    private Instant leavedAt;

    private ChatRoomParticipant(Long userId, ChatRoom chatRoom, long joinedMessageId) {
        Instant now = Instant.now();
        this.userId = userId;
        this.chatRoom = chatRoom;
        this.joinedMessageId = joinedMessageId;
        this.lastReadMessageId = joinedMessageId;
        this.isJoined = true;
        this.joinedAt = now;
        this.leavedAt = null;
    }

    public static ChatRoomParticipant create(Long userId, ChatRoom chatRoom) {
        long noMessageId = 0;
        return new ChatRoomParticipant(userId, chatRoom, noMessageId);
    }
}
