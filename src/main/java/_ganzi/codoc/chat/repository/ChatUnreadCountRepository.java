package _ganzi.codoc.chat.repository;

import java.time.Duration;
import java.util.OptionalLong;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class ChatUnreadCountRepository {

    private static final String TOTAL_UNREAD_PREFIX = "chat:unread:total:user:";
    private static final Duration TOTAL_UNREAD_TTL = Duration.ofDays(7);

    private final StringRedisTemplate stringRedisTemplate;

    public OptionalLong findTotalUnreadCount(Long userId) {
        String value = stringRedisTemplate.opsForValue().get(totalUnreadKey(userId));
        if (value == null) {
            return OptionalLong.empty();
        }

        try {
            return OptionalLong.of(Long.parseLong(value));
        } catch (NumberFormatException ignored) {
            return OptionalLong.empty();
        }
    }

    public void saveTotalUnreadCount(Long userId, long totalUnreadCount) {
        stringRedisTemplate
                .opsForValue()
                .set(totalUnreadKey(userId), String.valueOf(totalUnreadCount), TOTAL_UNREAD_TTL);
    }

    public long incrementTotalUnreadCount(Long userId, long delta) {
        String key = totalUnreadKey(userId);
        Long updated = stringRedisTemplate.opsForValue().increment(key, delta);
        if (updated == null) {
            return 0L;
        }
        if (updated < 0L) {
            saveTotalUnreadCount(userId, 0L);
            return 0L;
        }
        stringRedisTemplate.expire(key, TOTAL_UNREAD_TTL);
        return updated;
    }

    private String totalUnreadKey(Long userId) {
        return TOTAL_UNREAD_PREFIX + userId;
    }
}
