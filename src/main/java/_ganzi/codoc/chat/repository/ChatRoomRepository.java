package _ganzi.codoc.chat.repository;

import _ganzi.codoc.chat.domain.ChatRoom;
import _ganzi.codoc.chat.dto.ChatRoomListQueryResult;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    @Query(
            """
            select r
            from ChatRoom r
            where r.id = :roomId
              and r.isDeleted = false
            """)
    Optional<ChatRoom> findActiveById(@Param("roomId") Long roomId);

    @Query(
            """
            select new _ganzi.codoc.chat.dto.ChatRoomListQueryResult(
                r.id,
                r.title,
                case when r.password is not null then true else false end,
                r.participantCount,
                lm.createdAt
            )
            from ChatRoom r
            join ChatMessage lm on lm.chatRoom = r
            where r.isDeleted = false
              and lm.id = coalesce(
                    (
                        select max(tm.id)
                        from ChatMessage tm
                        where tm.chatRoom = r
                          and tm.type = _ganzi.codoc.chat.enums.ChatMessageType.TEXT
                    ),
                    (
                        select max(im.id)
                        from ChatMessage im
                        where im.chatRoom = r
                          and im.type = _ganzi.codoc.chat.enums.ChatMessageType.INIT
                    )
              )
              and (
                    :cursorOrderedAt is null
                    or lm.createdAt < :cursorOrderedAt
                    or (lm.createdAt = :cursorOrderedAt and r.id < :cursorRoomId)
              )
            order by lm.createdAt desc, r.id desc
            """)
    List<ChatRoomListQueryResult> findLatestChatRooms(
            @Param("cursorOrderedAt") Instant cursorOrderedAt,
            @Param("cursorRoomId") Long cursorRoomId,
            Pageable pageable);

    @Query(
            """
            select new _ganzi.codoc.chat.dto.ChatRoomListQueryResult(
                r.id,
                r.title,
                case when r.password is not null then true else false end,
                r.participantCount,
                lm.createdAt
            )
            from ChatRoom r
            join ChatMessage lm on lm.chatRoom = r
            where r.isDeleted = false
              and lower(r.title) like lower(concat('%', :keyword, '%'))
              and lm.id = coalesce(
                    (
                        select max(tm.id)
                        from ChatMessage tm
                        where tm.chatRoom = r
                          and tm.type = _ganzi.codoc.chat.enums.ChatMessageType.TEXT
                    ),
                    (
                        select max(im.id)
                        from ChatMessage im
                        where im.chatRoom = r
                          and im.type = _ganzi.codoc.chat.enums.ChatMessageType.INIT
                    )
              )
              and (
                    :cursorOrderedAt is null
                    or lm.createdAt < :cursorOrderedAt
                    or (lm.createdAt = :cursorOrderedAt and r.id < :cursorRoomId)
              )
            order by lm.createdAt desc, r.id desc
            """)
    List<ChatRoomListQueryResult> searchChatRoomsByKeyword(
            @Param("keyword") String keyword,
            @Param("cursorOrderedAt") Instant cursorOrderedAt,
            @Param("cursorRoomId") Long cursorRoomId,
            Pageable pageable);
}
