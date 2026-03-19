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
            select count(m.id) > 0
            from ChatMessage m
            where m.chatRoom.id = :roomId
              and m.id = :messageId
            """)
    boolean existsMessageInRoom(
            @Param("roomId") Long roomId, @Param("messageId") Long messageId);

    @Query(
            """
            select new _ganzi.codoc.chat.dto.ChatMessageListItem(
                m.id,
                m.senderId,
                u.nickname,
                a.imageUrl,
                m.type,
                m.content,
                null,
                null,
                m.createdAt
            )
            from ChatMessage m
            left join _ganzi.codoc.user.domain.User u
              on u.id = m.senderId
            left join u.avatar a
            where m.chatRoom.id = :roomId
              and m.type != _ganzi.codoc.chat.enums.ChatMessageType.INIT
              and (:cursorMessageId is null or m.id < :cursorMessageId)
            order by m.id desc
            """)
    List<ChatMessageListItem> findVisibleMessages(
            @Param("roomId") Long roomId,
            @Param("cursorMessageId") Long cursorMessageId,
            Pageable pageable);

    @Query(
            value =
                    """
            select count(*)
            from chat_message m
            where m.chat_room_id = :roomId
              and m.type = 'TEXT'
              and m.sender_id <> :userId
              and m.id > :fromMessageId
              and m.id <= :toMessageId
            """,
            nativeQuery = true)
    long countTextMessagesInRange(
            @Param("roomId") Long roomId,
            @Param("userId") Long userId,
            @Param("fromMessageId") long fromMessageId,
            @Param("toMessageId") long toMessageId);
}
