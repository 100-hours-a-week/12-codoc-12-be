package _ganzi.codoc.chat.repository;

import _ganzi.codoc.chat.domain.ChatRoomParticipant;
import _ganzi.codoc.chat.dto.ParticipantUnreadMessageCount;
import _ganzi.codoc.chat.dto.UserChatRoomListQueryResult;
import java.util.List;
import java.util.Optional;
import java.time.Instant;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatRoomParticipantRepository extends JpaRepository<ChatRoomParticipant, Long> {

    @Query(
            """
            select new _ganzi.codoc.chat.dto.UserChatRoomListQueryResult(
                p.id,
                r.id,
                r.title,
                r.participantCount,
                lm.content,
                lm.createdAt
            )
            from ChatRoomParticipant p
            join p.chatRoom r
            join ChatMessage lm on lm.chatRoom = r
            where p.userId = :userId
              and p.isJoined = true
              and r.isDeleted = false
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
    List<UserChatRoomListQueryResult> findLatestJoinedChatRoomsByUserId(
            @Param("userId") Long userId,
            @Param("cursorOrderedAt") Instant cursorOrderedAt,
            @Param("cursorRoomId") Long cursorRoomId,
            Pageable pageable);

    @Query(
            """
            select new _ganzi.codoc.chat.dto.UserChatRoomListQueryResult(
                p.id,
                r.id,
                r.title,
                r.participantCount,
                lm.content,
                lm.createdAt
            )
            from ChatRoomParticipant p
            join p.chatRoom r
            join ChatMessage lm on lm.chatRoom = r
            where p.userId = :userId
              and p.isJoined = true
              and r.isDeleted = false
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
    List<UserChatRoomListQueryResult> searchJoinedChatRoomsByKeyword(
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
              and :messageId <= p.chatRoom.lastMessageId
            """)
    void ackLastReadMessageId(
            @Param("roomId") Long roomId,
            @Param("userId") Long userId,
            @Param("messageId") long messageId);

    @Query(
            """
            select new _ganzi.codoc.chat.dto.ParticipantUnreadMessageCount(p.id, count(m.id))
            from ChatRoomParticipant p
            left join ChatMessage m
              on m.chatRoom = p.chatRoom
             and m.id > p.lastReadMessageId
             and m.senderId is not null
            where p.id in :participantIds
            group by p.id
            """)
    List<ParticipantUnreadMessageCount> countUnreadMessagesByParticipantIds(
            @Param("participantIds") List<Long> participantIds);

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
            select count(p.id) > 0
            from ChatRoomParticipant p
            where p.userId = :userId
              and p.isJoined = true
              and p.chatRoom.isDeleted = false
              and exists (
                    select 1
                    from ChatMessage m
                    where m.chatRoom = p.chatRoom
                      and m.id > coalesce(p.lastReadMessageId, 0)
                      and m.senderId is not null
              )
            """)
    boolean existsJoinedParticipantWithUnreadMessages(@Param("userId") Long userId);
}
