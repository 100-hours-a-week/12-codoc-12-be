package _ganzi.codoc.chat.repository;

import _ganzi.codoc.chat.domain.ChatRoom;
import _ganzi.codoc.chat.dto.ChatRoomListQueryResult;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(
            """
            select r
            from ChatRoom r
            where r.id = :roomId
              and r.isDeleted = false
            """)
    Optional<ChatRoom> findActiveByIdForUpdate(@Param("roomId") Long roomId);

    @Query(
            """
            select new _ganzi.codoc.chat.dto.ChatRoomListQueryResult(
                r.id,
                r.title,
                case when r.password is not null then true else false end,
                count(joinedParticipant.id),
                lm.createdAt
            )
            from ChatRoom r
            join ChatMessage lm on lm.chatRoom = r
            left join ChatRoomParticipant joinedParticipant
              on joinedParticipant.chatRoom = r
             and joinedParticipant.isJoined = true
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
            group by r.id, r.title, r.password, lm.createdAt
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
                count(joinedParticipant.id),
                lm.createdAt
            )
            from ChatRoom r
            join ChatMessage lm on lm.chatRoom = r
            left join ChatRoomParticipant joinedParticipant
              on joinedParticipant.chatRoom = r
             and joinedParticipant.isJoined = true
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
            group by r.id, r.title, r.password, lm.createdAt
            order by lm.createdAt desc, r.id desc
            """)
    List<ChatRoomListQueryResult> searchChatRoomsByKeyword(
            @Param("keyword") String keyword,
            @Param("cursorOrderedAt") Instant cursorOrderedAt,
            @Param("cursorRoomId") Long cursorRoomId,
            Pageable pageable);

    @Modifying
    @Query(
            """
            update ChatRoom r
            set r.isDeleted = true,
                r.deletedAt = :deletedAt
            where r.id = :roomId
              and r.isDeleted = false
              and not exists (
                    select 1
                    from ChatRoomParticipant p
                    where p.chatRoom = r
                      and p.isJoined = true
              )
            """)
    void markDeletedIfEmpty(@Param("roomId") Long roomId, @Param("deletedAt") Instant deletedAt);
}
