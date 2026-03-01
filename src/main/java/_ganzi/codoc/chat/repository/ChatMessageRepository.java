package _ganzi.codoc.chat.repository;

import _ganzi.codoc.chat.domain.ChatMessage;
import _ganzi.codoc.chat.dto.ChatMessageListItem;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    @Query(
            """
            select new _ganzi.codoc.chat.dto.ChatMessageListItem(
                m.id,
                m.senderId,
                u.nickname,
                a.imageUrl,
                m.type,
                m.content,
                m.chatRoom.participantCount,
                m.createdAt
            )
            from ChatMessage m
            left join _ganzi.codoc.user.domain.User u
              on u.id = m.senderId
            left join u.avatar a
            where m.chatRoom.id = :roomId
              and m.id >= :joinedMessageId
              and m.type != _ganzi.codoc.chat.enums.ChatMessageType.INIT
              and (:cursorMessageId is null or m.id < :cursorMessageId)
            order by m.id desc
            """)
    List<ChatMessageListItem> findVisibleMessages(
            @Param("roomId") Long roomId,
            @Param("joinedMessageId") long joinedMessageId,
            @Param("cursorMessageId") Long cursorMessageId,
            Pageable pageable);
}
