package _ganzi.codoc.chat.config;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class ChatRoomSubscriptionRegistry {

    private final Map<Long, Set<Long>> roomSubscribers = new ConcurrentHashMap<>();
    private final Map<String, SessionInfo> sessionRegistry = new ConcurrentHashMap<>();

    public void addSubscription(String sessionId, Long userId, Long roomId) {
        sessionRegistry
                .computeIfAbsent(sessionId, k -> new SessionInfo(userId, ConcurrentHashMap.newKeySet()))
                .roomIds()
                .add(roomId);

        roomSubscribers.computeIfAbsent(roomId, k -> ConcurrentHashMap.newKeySet()).add(userId);
    }

    public void removeSubscription(String sessionId, Long roomId) {
        SessionInfo info = sessionRegistry.get(sessionId);
        if (info == null) {
            return;
        }

        info.roomIds().remove(roomId);
        Set<Long> subscribers = roomSubscribers.get(roomId);
        if (subscribers != null) {
            subscribers.remove(info.userId());
        }
    }

    public void removeSession(String sessionId) {
        SessionInfo info = sessionRegistry.remove(sessionId);
        if (info == null) {
            return;
        }

        for (Long roomId : info.roomIds()) {
            Set<Long> subscribers = roomSubscribers.get(roomId);
            if (subscribers != null) {
                subscribers.remove(info.userId());
            }
        }
    }

    public Set<Long> getSubscriberUserIds(Long roomId) {
        Set<Long> subscribers = roomSubscribers.get(roomId);
        return subscribers == null ? Collections.emptySet() : Collections.unmodifiableSet(subscribers);
    }

    record SessionInfo(Long userId, Set<Long> roomIds) {}
}
