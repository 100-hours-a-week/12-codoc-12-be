package _ganzi.codoc.notification.service;

import _ganzi.codoc.notification.domain.NotificationConsumeLog;
import _ganzi.codoc.notification.enums.NotificationConsumeStatus;
import _ganzi.codoc.notification.repository.NotificationConsumeLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationConsumeLogService {

    private final NotificationConsumeLogRepository notificationConsumeLogRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ConsumeDecision beginConsume(String messageId, String channel) {
        if (messageId == null || messageId.isBlank()) {
            return ConsumeDecision.PROCEED;
        }
        NotificationConsumeLog existing =
                notificationConsumeLogRepository.findByMessageIdAndChannel(messageId, channel).orElse(null);
        if (existing != null) {
            return existing.getStatus() == NotificationConsumeStatus.DONE
                    ? ConsumeDecision.SKIP
                    : ConsumeDecision.PROCEED;
        }
        return createPendingIfAbsent(messageId, channel)
                ? ConsumeDecision.PROCEED
                : ConsumeDecision.SKIP;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markDone(String messageId, String channel) {
        if (messageId == null || messageId.isBlank()) {
            return;
        }
        NotificationConsumeLog log =
                notificationConsumeLogRepository
                        .findByMessageIdAndChannel(messageId, channel)
                        .orElseGet(() -> NotificationConsumeLog.createDone(messageId, channel));
        log.markDone();
        notificationConsumeLogRepository.save(log);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(String messageId, String channel, Exception exception) {
        if (messageId == null || messageId.isBlank()) {
            return;
        }
        NotificationConsumeLog log =
                notificationConsumeLogRepository
                        .findByMessageIdAndChannel(messageId, channel)
                        .orElseGet(() -> NotificationConsumeLog.createPending(messageId, channel));
        String errorMessage = exception == null ? null : exception.getClass().getSimpleName();
        log.markFailed(errorMessage);
        notificationConsumeLogRepository.save(log);
    }

    private boolean createPendingIfAbsent(String messageId, String channel) {
        try {
            notificationConsumeLogRepository.saveAndFlush(
                    NotificationConsumeLog.createPending(messageId, channel));
            return true;
        } catch (DataIntegrityViolationException exception) {
            log.info("skip duplicated notification event. messageId={}, channel={}", messageId, channel);
            return false;
        }
    }

    public enum ConsumeDecision {
        PROCEED,
        SKIP
    }
}
