package _ganzi.codoc.chat.repository;

import _ganzi.codoc.chat.domain.ChatRoomParticipant;
import java.time.Instant;
import java.util.List;

import _ganzi.codoc.chat.dto.ParticipantUnreadMessageCount;
import _ganzi.codoc.chat.enums.ChatMessageType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
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
