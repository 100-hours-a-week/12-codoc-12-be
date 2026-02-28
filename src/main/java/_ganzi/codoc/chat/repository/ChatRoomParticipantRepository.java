package _ganzi.codoc.chat.repository;

import _ganzi.codoc.chat.domain.ChatRoomParticipant;
import _ganzi.codoc.chat.dto.ParticipantUnreadMessageCount;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatRoomParticipantRepository extends JpaRepository<ChatRoomParticipant, Long> {

    @Query(
            """
            select p
            from ChatRoomParticipant p
            join fetch p.chatRoom r
            where p.userId = :userId
              and p.isJoined = true
              and r.isDeleted = false
              and (
                    :cursorOrderedAt is null
                    or r.lastMessageAt < :cursorOrderedAt
                    or (r.lastMessageAt = :cursorOrderedAt and r.id < :cursorRoomId)
              )
            order by r.lastMessageAt desc, r.id desc
            """)
    List<ChatRoomParticipant> findLatestJoinedChatRoomsByUserId(
            @Param("userId") Long userId,
            @Param("cursorOrderedAt") Instant cursorOrderedAt,
            @Param("cursorRoomId") Long cursorRoomId,
            Pageable pageable);

    @Query(
            """
            select p
            from ChatRoomParticipant p
            join fetch p.chatRoom r
            where p.userId = :userId
              and p.isJoined = true
              and r.isDeleted = false
              and lower(r.title) like lower(concat('%', :keyword, '%'))
              and (
                    :cursorOrderedAt is null
                    or r.lastMessageAt < :cursorOrderedAt
                    or (r.lastMessageAt = :cursorOrderedAt and r.id < :cursorRoomId)
              )
            order by r.lastMessageAt desc, r.id desc
            """)
    List<ChatRoomParticipant> searchJoinedChatRoomsByKeyword(
            @Param("userId") Long userId,
            @Param("keyword") String keyword,
            @Param("cursorOrderedAt") Instant cursorOrderedAt,
            @Param("cursorRoomId") Long cursorRoomId,
            Pageable pageable);

    @Query(
            """
            select r.lastMessageId
            from ChatRoomParticipant p
            join p.chatRoom r
            where p.userId = :userId
              and p.chatRoom.id = :roomId
              and p.isJoined = true
            """)
    Long findLastMessageIdByJoinedParticipant(
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
              and p.userId in :userIds
              and p.isJoined = true
            """)
    void updateLastReadMessageId(
            @Param("roomId") Long roomId,
            @Param("userIds") Collection<Long> userIds,
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
}
