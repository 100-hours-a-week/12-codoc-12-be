package _ganzi.codoc.chat.repository;

import _ganzi.codoc.chat.domain.ChatRoomLatestMessage;
import java.time.Instant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatRoomLatestMessageRepository extends JpaRepository<ChatRoomLatestMessage, Long> {

    @Modifying
    @Query(
            """
            update ChatRoomLatestMessage lm
            set lm.latestTextMessageId = :messageId,
                lm.latestMessagePreview = :messagePreview,
                lm.latestMessageAt = :messageAt
            where lm.chatRoomId = :roomId
              and (lm.latestTextMessageId is null or lm.latestTextMessageId < :messageId)
            """)
    void updateLatestTextMessageIfNewer(
            @Param("roomId") Long roomId,
            @Param("messageId") Long messageId,
            @Param("messagePreview") String messagePreview,
            @Param("messageAt") Instant messageAt);
}
