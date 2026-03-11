package _ganzi.codoc.notification.service;

import _ganzi.codoc.notification.dto.NotificationMessageItem;
import _ganzi.codoc.notification.mq.NotificationEventPublisher;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationDispatchService {

    private final ObjectProvider<NotificationEventPublisher> notificationEventPublisherProvider;
    private final NotificationSendService notificationSendService;
    private final PushNotificationSendService pushNotificationSendService;

    public void dispatch(Long userId, NotificationMessageItem messageItem) {
        NotificationEventPublisher publisher = notificationEventPublisherProvider.getIfAvailable();
        if (publisher != null) {
            try {
                publisher.publish(userId, messageItem);
                return;
            } catch (Exception exception) {
                log.warn(
                        "notification mq publish failed. fallback to sync. userId={}, type={}",
                        userId,
                        messageItem.type(),
                        exception);
            }
        }

        notificationSendService.send(userId, messageItem);
        pushNotificationSendService.send(userId, messageItem);
    }

    public void dispatchAfterCommit(Long userId, NotificationMessageItem messageItem) {
        if (TransactionSynchronizationManager.isSynchronizationActive()
                && TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            dispatch(userId, messageItem);
                        }
                    });
            return;
        }
        dispatch(userId, messageItem);
    }

    public void dispatchBatchAfterCommit(
            List<Long> userIds, NotificationMessageItem messageItem, int batchSize) {
        if (userIds == null || userIds.isEmpty()) {
            return;
        }
        int resolvedBatchSize = batchSize <= 0 ? 500 : batchSize;
        if (TransactionSynchronizationManager.isSynchronizationActive()
                && TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            dispatchBatch(userIds, messageItem, resolvedBatchSize);
                        }
                    });
            return;
        }
        dispatchBatch(userIds, messageItem, resolvedBatchSize);
    }

    private void dispatchBatch(
            List<Long> userIds, NotificationMessageItem messageItem, int batchSize) {
        for (int start = 0; start < userIds.size(); start += batchSize) {
            int end = Math.min(start + batchSize, userIds.size());
            for (Long userId : userIds.subList(start, end)) {
                dispatch(userId, messageItem);
            }
        }
    }
}
