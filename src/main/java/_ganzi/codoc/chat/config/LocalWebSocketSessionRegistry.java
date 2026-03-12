package _ganzi.codoc.chat.config;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class LocalWebSocketSessionRegistry {

    private final Map<String, Long> sessionUserIds = new ConcurrentHashMap<>();
    private final Map<Long, Set<String>> userSessions = new ConcurrentHashMap<>();

    public void addSession(String sessionId, Long userId) {
        sessionUserIds.put(sessionId, userId);
        userSessions.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(sessionId);
    }

    public void removeSession(String sessionId) {
        Long userId = sessionUserIds.remove(sessionId);
        if (userId == null) {
            return;
        }

        Set<String> sessions = userSessions.get(userId);
        if (sessions != null) {
            sessions.remove(sessionId);
            if (sessions.isEmpty()) {
                userSessions.remove(userId);
            }
        }
    }

    public Long findUserId(String sessionId) {
        return sessionUserIds.get(sessionId);
    }

    public Map<String, Long> snapshotSessionUsers() {
        return Map.copyOf(sessionUserIds);
    }

    public boolean isConnected(Long userId) {
        Set<String> sessions = userSessions.get(userId);
        return sessions != null && !sessions.isEmpty();
    }
}
