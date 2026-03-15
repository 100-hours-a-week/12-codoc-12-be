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
                lm.latestMessageAt
            )
            from ChatRoom r
            join _ganzi.codoc.chat.domain.ChatRoomLatestMessage lm
              on lm.chatRoom = r
            left join ChatRoomParticipant joinedParticipant
              on joinedParticipant.chatRoom = r
             and joinedParticipant.isJoined = true
            where r.isDeleted = false
              and (
                    :cursorOrderedAt is null
                    or lm.latestMessageAt < :cursorOrderedAt
                    or (lm.latestMessageAt = :cursorOrderedAt and r.id < :cursorRoomId)
              )
            group by r.id, r.title, r.password, lm.latestMessageAt
            order by lm.latestMessageAt desc, r.id desc
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
                lm.latestMessageAt
            )
            from ChatRoom r
            join _ganzi.codoc.chat.domain.ChatRoomLatestMessage lm
              on lm.chatRoom = r
            left join ChatRoomParticipant joinedParticipant
              on joinedParticipant.chatRoom = r
             and joinedParticipant.isJoined = true
            where r.isDeleted = false
              and r.title like concat('%', :keyword, '%')
              and (
                    :cursorOrderedAt is null
                    or lm.latestMessageAt < :cursorOrderedAt
                    or (lm.latestMessageAt = :cursorOrderedAt and r.id < :cursorRoomId)
              )
            group by r.id, r.title, r.password, lm.latestMessageAt
            order by lm.latestMessageAt desc, r.id desc
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
