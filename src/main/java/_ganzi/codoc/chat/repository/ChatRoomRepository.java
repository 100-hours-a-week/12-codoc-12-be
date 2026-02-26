package _ganzi.codoc.chat.repository;

import _ganzi.codoc.chat.domain.ChatRoom;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    @Query(
            """
            select r
            from ChatRoom r
            where r.isDeleted = false
              and lower(r.title) like lower(concat('%', :keyword, '%'))
              and (
                    :cursorOrderedAt is null
                    or r.lastMessageAt < :cursorOrderedAt
                    or (r.lastMessageAt = :cursorOrderedAt and r.id < :cursorRoomId)
              )
            order by r.lastMessageAt desc, r.id desc
            """)
    List<ChatRoom> searchChatRoomsByKeyword(
            @Param("keyword") String keyword,
            @Param("cursorOrderedAt") Instant cursorOrderedAt,
            @Param("cursorRoomId") Long cursorRoomId,
            Pageable pageable);
}
