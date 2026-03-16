package _ganzi.codoc.chat.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import _ganzi.codoc.chat.domain.ChatRoomParticipant;
import _ganzi.codoc.chat.dto.ParticipantReadPosition;
import _ganzi.codoc.chat.dto.ParticipantUnreadMessageCountRow;
import _ganzi.codoc.chat.dto.RoomParticipantCount;
import _ganzi.codoc.chat.dto.UserChatRoomListRow;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatRoomParticipantRepository extends JpaRepository<ChatRoomParticipant, Long> {

    @Query(
            """
            select
                p.id as participantId,
                r.id as roomId,
                r.title as title,
                lm.latestMessagePreview as lastMessagePreview,
                lm.latestMessageAt as lastMessageAt
            from ChatRoomParticipant p
            join p.chatRoom r
            join _ganzi.codoc.chat.domain.ChatRoomLatestMessage lm
              on lm.chatRoom = r
            where p.userId = :userId
              and p.isJoined = true
              and r.isDeleted = false
              and (
                    :cursorOrderedAt is null
                    or lm.latestMessageAt < :cursorOrderedAt
                    or (lm.latestMessageAt = :cursorOrderedAt and r.id < :cursorRoomId)
              )
            order by lm.latestMessageAt desc, r.id desc
            """)
    List<UserChatRoomListRow> findLatestJoinedChatRoomsByUserId(
            @Param("userId") Long userId,
            @Param("cursorOrderedAt") Instant cursorOrderedAt,
            @Param("cursorRoomId") Long cursorRoomId,
            Pageable pageable);

    @Query(
            """
            select
                p.id as participantId,
                r.id as roomId,
                r.title as title,
                lm.latestMessagePreview as lastMessagePreview,
                lm.latestMessageAt as lastMessageAt
            from ChatRoomParticipant p
            join p.chatRoom r
            join _ganzi.codoc.chat.domain.ChatRoomLatestMessage lm
              on lm.chatRoom = r
            where p.userId = :userId
              and p.isJoined = true
              and r.isDeleted = false
              and r.title like concat('%', :keyword, '%')
              and (
                    :cursorOrderedAt is null
                    or lm.latestMessageAt < :cursorOrderedAt
                    or (lm.latestMessageAt = :cursorOrderedAt and r.id < :cursorRoomId)
              )
            order by lm.latestMessageAt desc, r.id desc
            """)
    List<UserChatRoomListRow> searchJoinedChatRoomsByKeyword(
            @Param("userId") Long userId,
            @Param("keyword") String keyword,
            @Param("cursorOrderedAt") Instant cursorOrderedAt,
            @Param("cursorRoomId") Long cursorRoomId,
            Pageable pageable);

    @Query(
            """
            select count(p.id) > 0
            from ChatRoomParticipant p
            where p.userId = :userId
              and p.chatRoom.id = :roomId
              and p.isJoined = true
            """)
    boolean existsJoinedParticipant(
            @Param("userId") Long userId, @Param("roomId") Long roomId);

    @Query(
            """
            select count(p.id)
            from ChatRoomParticipant p
            where p.chatRoom.id = :roomId
              and p.isJoined = true
            """)
    long countJoinedParticipantsByRoomId(@Param("roomId") Long roomId);

    @Query(
            """
              select p
              from ChatRoomParticipant p
              where p.userId = :userId
              and p.chatRoom.id = :roomId
              and p.isJoined = true
            """)
    Optional<ChatRoomParticipant> findJoinedParticipant(
      @Param("userId") Long userId, @Param("roomId") Long roomId);

    @Query(
            """
              select p
              from ChatRoomParticipant p
              where p.userId = :userId
                and p.chatRoom.id = :roomId
            """)
    Optional<ChatRoomParticipant> findByUserIdAndRoomId(
            @Param("userId") Long userId, @Param("roomId") Long roomId);

    @Query(
            """
            select p.userId
            from ChatRoomParticipant p
            where p.chatRoom.id = :roomId
              and p.isJoined = true
            """)
    List<Long> findJoinedUserIdsByRoomId(@Param("roomId") Long roomId);

    @Modifying
    @Query(
            """
            update ChatRoomParticipant p
            set p.lastReadMessageId = :messageId
            where p.chatRoom.id = :roomId
              and p.userId = :userId
              and p.isJoined = true
              and p.lastReadMessageId < :messageId
            """)
    void ackLastReadMessageId(
            @Param("roomId") Long roomId,
            @Param("userId") Long userId,
            @Param("messageId") long messageId);

    @Query(
            value =
                    """
            select
                p.id as participantId,
                (
                    select count(*)
                    from (
                        select m.id
                        from chat_message m
                        where m.chat_room_id = p.chat_room_id
                          and m.id > coalesce(p.last_read_message_id, 0)
                          and m.type = 'TEXT'
                        limit 1000
                    ) limited_unread
                ) as unreadCount
            from chat_room_participant p
            where p.id in (:participantIds)
            """,
            nativeQuery = true)
    List<ParticipantUnreadMessageCountRow> countUnreadMessagesByParticipantIds(
            @Param("participantIds") List<Long> participantIds);

    @Query(
            value =
                    """
            select count(*)
            from (
                select 1
                from chat_room_participant p
                join chat_room r
                  on r.id = p.chat_room_id
                 and r.is_deleted = 0
                join chat_message m
                  on m.chat_room_id = p.chat_room_id
                 and m.id > coalesce(p.last_read_message_id, 0)
                 and m.type = 'TEXT'
                where p.user_id = :userId
                  and p.is_joined = 1
                limit 1000
            ) total_unread
            """,
            nativeQuery = true)
    long countTotalUnreadMessagesByUserId(@Param("userId") Long userId);

    @Query(
            """
            select new _ganzi.codoc.chat.dto.RoomParticipantCount(p.chatRoom.id, count(p.id))
            from ChatRoomParticipant p
            where p.chatRoom.id in :roomIds
              and p.isJoined = true
            group by p.chatRoom.id
            """)
    List<RoomParticipantCount> countJoinedParticipantsByRoomIds(
            @Param("roomIds") List<Long> roomIds);

    @Query(
            """
            select p
            from ChatRoomParticipant p
            join fetch p.chatRoom r
            where p.userId = :userId
              and p.isJoined = true
              and r.isDeleted = false
            """)
    List<ChatRoomParticipant> findAllJoinedByUserId(@Param("userId") Long userId);

    @Query(
            """
            select new _ganzi.codoc.chat.dto.ParticipantReadPosition(p.userId, p.lastReadMessageId)
            from ChatRoomParticipant p
            where p.chatRoom.id = :roomId
              and p.isJoined = true
            """)
    List<ParticipantReadPosition> findJoinedParticipantReadPositions(@Param("roomId") Long roomId);
}
