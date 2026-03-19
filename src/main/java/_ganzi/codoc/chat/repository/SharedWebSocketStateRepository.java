package _ganzi.codoc.chat.repository;

import _ganzi.codoc.chat.config.ChatWebSocketProperties;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class SharedWebSocketStateRepository {

    private static final String SESSION_PREFIX = "ws:session:";
    private static final String USER_SESSIONS_PREFIX = "ws:user:";
    private static final String ROOM_SESSIONS_PREFIX = "ws:room:";
    private static final String ROOM_ACTIVE_SESSIONS_PREFIX = "ws:room-active:";
    private static final String PRESENCE_PREFIX = "ws:presence:user:";
    private static final String SESSION_USER_ID_FIELD = "userId";
    private static final String SESSION_SERVER_ID_FIELD = "serverId";

    private final StringRedisTemplate stringRedisTemplate;
    private final ChatWebSocketProperties properties;

    public void registerSession(String sessionId, Long userId) {
        String sessionKey = sessionKey(sessionId);

        stringRedisTemplate.delete(sessionKey);
        stringRedisTemplate
                .opsForHash()
                .putAll(
                        sessionKey,
                        Map.of(
                                SESSION_USER_ID_FIELD, String.valueOf(userId),
                                SESSION_SERVER_ID_FIELD, properties.serverId()));
        stringRedisTemplate.expire(sessionKey, properties.sessionTtl());
        stringRedisTemplate.opsForSet().add(userSessionsKey(userId), sessionId);
        touchPresence(userId);
    }

    public void touchSession(String sessionId) {
        stringRedisTemplate.expire(sessionKey(sessionId), properties.sessionTtl());
    }

    public void touchPresence(Long userId) {
        stringRedisTemplate
                .opsForValue()
                .set(presenceKey(userId), properties.serverId(), properties.presenceTtl());
    }

    public void addRoomSession(Long roomId, String sessionId) {
        stringRedisTemplate.opsForSet().add(roomSessionsKey(roomId), sessionId);
    }

    public void addActiveRoomSession(Long roomId, String sessionId) {
        stringRedisTemplate.opsForSet().add(activeRoomSessionsKey(roomId), sessionId);
    }

    public void removeRoomSession(Long roomId, String sessionId) {
        stringRedisTemplate.opsForSet().remove(roomSessionsKey(roomId), sessionId);
        deleteIfEmpty(roomSessionsKey(roomId));
    }

    public void removeActiveRoomSession(Long roomId, String sessionId) {
        stringRedisTemplate.opsForSet().remove(activeRoomSessionsKey(roomId), sessionId);
        deleteIfEmpty(activeRoomSessionsKey(roomId));
    }

    public void removeActiveRoomSessions(Long roomId, Collection<String> sessionIds) {
        if (sessionIds.isEmpty()) {
            return;
        }
        stringRedisTemplate.opsForSet().remove(activeRoomSessionsKey(roomId), sessionIds.toArray());
        deleteIfEmpty(activeRoomSessionsKey(roomId));
    }

    public void removeSession(String sessionId) {
        stringRedisTemplate.delete(sessionKey(sessionId));
    }

    public void removeUserSession(Long userId, String sessionId) {
        stringRedisTemplate.opsForSet().remove(userSessionsKey(userId), sessionId);
        deleteIfEmpty(userSessionsKey(userId));
    }

    public void removeUserSessions(Long userId, Collection<String> sessionIds) {
        if (sessionIds.isEmpty()) {
            return;
        }
        stringRedisTemplate.opsForSet().remove(userSessionsKey(userId), sessionIds.toArray());
        deleteIfEmpty(userSessionsKey(userId));
    }

    public void deletePresence(Long userId) {
        stringRedisTemplate.delete(presenceKey(userId));
    }

    public boolean hasAnyUserSession(Long userId) {
        Long count = stringRedisTemplate.opsForSet().size(userSessionsKey(userId));
        return count != null && count > 0;
    }

    public boolean sessionExists(String sessionId) {
        Boolean exists = stringRedisTemplate.hasKey(sessionKey(sessionId));
        return Boolean.TRUE.equals(exists);
    }

    public Set<String> findUserSessionIds(Long userId) {
        Set<String> members = stringRedisTemplate.opsForSet().members(userSessionsKey(userId));
        return members == null ? Set.of() : members;
    }

    public Set<String> findActiveRoomSessionIds(Long roomId) {
        Set<String> members = stringRedisTemplate.opsForSet().members(activeRoomSessionsKey(roomId));
        return members == null ? Set.of() : members;
    }

    public Optional<Long> findSessionUserId(String sessionId) {
        String sessionKey = sessionKey(sessionId);
        Object hashUserId = stringRedisTemplate.opsForHash().get(sessionKey, SESSION_USER_ID_FIELD);
        if (hashUserId instanceof String userIdToken) {
            return parseUserId(userIdToken);
        }
        return Optional.empty();
    }

    private Optional<Long> parseUserId(String userIdToken) {
        try {
            return Optional.of(Long.parseLong(userIdToken));
        } catch (NumberFormatException ignored) {
            return Optional.empty();
        }
    }

    private void deleteIfEmpty(String key) {
        Long size = stringRedisTemplate.opsForSet().size(key);
        if (size != null && size == 0L) {
            stringRedisTemplate.delete(key);
        }
    }

    private String sessionKey(String sessionId) {
        return SESSION_PREFIX + sessionId;
    }

    private String userSessionsKey(Long userId) {
        return USER_SESSIONS_PREFIX + userId + ":sessions";
    }

    private String roomSessionsKey(Long roomId) {
        return ROOM_SESSIONS_PREFIX + roomId + ":sessions";
    }

    private String activeRoomSessionsKey(Long roomId) {
        return ROOM_ACTIVE_SESSIONS_PREFIX + roomId + ":sessions";
    }

    private String presenceKey(Long userId) {
        return PRESENCE_PREFIX + userId;
    }
}
