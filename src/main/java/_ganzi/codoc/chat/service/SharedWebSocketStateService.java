package _ganzi.codoc.chat.service;

import _ganzi.codoc.chat.repository.SharedWebSocketStateRepository;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class SharedWebSocketStateService {

    private final SharedWebSocketStateRepository repository;

    public void registerSession(String sessionId, Long userId) {
        try {
            repository.registerSession(sessionId, userId);
        } catch (RuntimeException e) {
            log.warn("shared session register failed. sessionId={}", sessionId, e);
        }
    }

    public void touchSession(String sessionId, Long userId) {
        try {
            repository.touchSession(sessionId);
            repository.touchPresence(userId);
        } catch (RuntimeException e) {
            log.warn("shared session touch failed. sessionId={}", sessionId, e);
        }
    }

    public void addRoomSubscription(String sessionId, Long roomId) {
        try {
            repository.addRoomSession(roomId, sessionId);
        } catch (RuntimeException e) {
            log.warn("shared room subscribe failed. sessionId={}, roomId={}", sessionId, roomId, e);
        }
    }

    public void removeRoomSubscription(String sessionId, Long roomId) {
        try {
            repository.removeRoomSession(roomId, sessionId);
            repository.removeActiveRoomSession(roomId, sessionId);
        } catch (RuntimeException e) {
            log.warn("shared room unsubscribe failed. sessionId={}, roomId={}", sessionId, roomId, e);
        }
    }

    public void activateRoomView(String sessionId, Long roomId) {
        try {
            repository.addActiveRoomSession(roomId, sessionId);
        } catch (RuntimeException e) {
            log.warn("shared room view activate failed. sessionId={}, roomId={}", sessionId, roomId, e);
        }
    }

    public void deactivateRoomView(String sessionId, Long roomId) {
        try {
            repository.removeActiveRoomSession(roomId, sessionId);
        } catch (RuntimeException e) {
            log.warn("shared room view deactivate failed. sessionId={}, roomId={}", sessionId, roomId, e);
        }
    }

    public void removeSession(String sessionId, Long userId, Collection<Long> roomIds) {
        try {
            Long resolvedUserId = userId;
            if (resolvedUserId == null) {
                resolvedUserId = repository.findSessionUserId(sessionId).orElse(null);
            }

            repository.removeSession(sessionId);
            if (resolvedUserId != null) {
                repository.removeUserSession(resolvedUserId, sessionId);
                if (repository.hasAnyUserSession(resolvedUserId)) {
                    repository.touchPresence(resolvedUserId);
                } else {
                    repository.deletePresence(resolvedUserId);
                }
            }

            for (Long roomId : roomIds) {
                repository.removeRoomSession(roomId, sessionId);
                repository.removeActiveRoomSession(roomId, sessionId);
            }
        } catch (RuntimeException e) {
            log.warn("shared session cleanup failed. sessionId={}", sessionId, e);
        }
    }

    public boolean isConnected(Long userId) {
        try {
            Set<String> sessionIds = repository.findUserSessionIds(userId);
            if (sessionIds.isEmpty()) {
                repository.deletePresence(userId);
                return false;
            }

            boolean hasActiveSession = false;
            Set<String> staleSessionIds = new HashSet<>();
            for (String sessionId : sessionIds) {
                if (repository.sessionExists(sessionId)) {
                    hasActiveSession = true;
                } else {
                    staleSessionIds.add(sessionId);
                }
            }

            if (!staleSessionIds.isEmpty()) {
                repository.removeUserSessions(userId, staleSessionIds);
            }

            if (hasActiveSession) {
                repository.touchPresence(userId);
                return true;
            }

            repository.deletePresence(userId);
            return false;
        } catch (RuntimeException e) {
            log.warn("shared connected lookup failed. userId={}", userId, e);
            return false;
        }
    }

    public Set<Long> getActiveRoomViewerUserIds(Long roomId) {
        try {
            Set<String> sessionIds = repository.findActiveRoomSessionIds(roomId);

            Set<Long> activeUserIds = new HashSet<>();
            Set<String> staleSessionIds = new HashSet<>();

            for (String sessionId : sessionIds) {
                repository
                        .findSessionUserId(sessionId)
                        .ifPresentOrElse(activeUserIds::add, () -> staleSessionIds.add(sessionId));
            }

            repository.removeActiveRoomSessions(roomId, staleSessionIds);

            return Set.copyOf(activeUserIds);
        } catch (RuntimeException e) {
            log.warn("shared active room viewer lookup failed. roomId={}", roomId, e);
            return Set.of();
        }
    }
}
