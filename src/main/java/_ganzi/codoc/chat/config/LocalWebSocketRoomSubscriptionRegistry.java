package _ganzi.codoc.chat.config;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class LocalWebSocketRoomSubscriptionRegistry {

    private final Map<Long, Set<String>> roomSessions = new ConcurrentHashMap<>();
    private final Map<String, SessionInfo> sessionRegistry = new ConcurrentHashMap<>();

    public void addSubscription(String sessionId, Long userId, Long roomId) {
        sessionRegistry
                .computeIfAbsent(sessionId, k -> new SessionInfo(userId, ConcurrentHashMap.newKeySet()))
                .roomIds()
                .add(roomId);

        roomSessions.computeIfAbsent(roomId, k -> ConcurrentHashMap.newKeySet()).add(sessionId);
    }

    public void removeSubscription(String sessionId, Long roomId) {
        SessionInfo info = sessionRegistry.get(sessionId);
        if (info == null) {
            return;
        }

        info.roomIds().remove(roomId);
        Set<String> sessions = roomSessions.get(roomId);
        if (sessions != null) {
            sessions.remove(sessionId);
            if (sessions.isEmpty()) {
                roomSessions.remove(roomId);
            }
        }
    }

    public void removeSession(String sessionId) {
        SessionInfo info = sessionRegistry.remove(sessionId);
        if (info == null) {
            return;
        }

        for (Long roomId : info.roomIds()) {
            Set<String> sessions = roomSessions.get(roomId);
            if (sessions != null) {
                sessions.remove(sessionId);
                if (sessions.isEmpty()) {
                    roomSessions.remove(roomId);
                }
            }
        }
    }

    public Set<Long> getSessionRoomIds(String sessionId) {
        SessionInfo info = sessionRegistry.get(sessionId);
        if (info == null || info.roomIds().isEmpty()) {
            return Collections.emptySet();
        }
        return Collections.unmodifiableSet(new HashSet<>(info.roomIds()));
    }

    public Set<Long> getSubscriberUserIds(Long roomId) {
        Set<String> sessions = roomSessions.get(roomId);
        if (sessions == null || sessions.isEmpty()) {
            return Collections.emptySet();
        }

        Set<Long> subscribers = new HashSet<>();
        for (String sessionId : sessions) {
            SessionInfo info = sessionRegistry.get(sessionId);
            if (info != null) {
                subscribers.add(info.userId());
            }
        }
        return Collections.unmodifiableSet(subscribers);
    }

    record SessionInfo(Long userId, Set<Long> roomIds) {}
}
