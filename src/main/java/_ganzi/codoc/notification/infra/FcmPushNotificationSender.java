package _ganzi.codoc.notification.infra;

import _ganzi.codoc.notification.dto.NotificationMessageItem;
import _ganzi.codoc.notification.enums.PushNotificationSendResult;
import _ganzi.codoc.notification.service.PushNotificationSender;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MessagingErrorCode;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@RequiredArgsConstructor
@Component
public class FcmPushNotificationSender implements PushNotificationSender {

    private static final JsonMapper JSON_MAPPER = JsonMapper.builder().build();

    private final ObjectProvider<FirebaseMessaging> firebaseMessagingProvider;

    @Override
    public PushNotificationSendResult send(NotificationMessageItem messageItem, String pushToken) {

        FirebaseMessaging firebaseMessaging = firebaseMessagingProvider.getIfAvailable();
        if (firebaseMessaging == null) {
            log.info("Skip push notification send because FCM is unavailable");
            return PushNotificationSendResult.FAILED;
        }

        Message.Builder messageBuilder =
                Message.builder()
                        .setToken(pushToken)
                        .setNotification(
                                com.google.firebase.messaging.Notification.builder()
                                        .setTitle(messageItem.title())
                                        .setBody(messageItem.body())
                                        .build())
                        .putData("type", messageItem.type().name());

        if (messageItem.type().getLinkCode() != null) {
            messageBuilder.putData("linkCode", messageItem.type().getLinkCode().name());
        }

        if (messageItem.linkParams() != null && !messageItem.linkParams().isEmpty()) {
            String linkParamsJson = toJson(messageItem.linkParams());
            if (StringUtils.hasText(linkParamsJson)) {
                messageBuilder.putData("linkParams", linkParamsJson);
            }
        }

        try {
            firebaseMessaging.send(messageBuilder.build());
            return PushNotificationSendResult.SUCCESS;
        } catch (FirebaseMessagingException exception) {
            if (isInvalidTokenError(exception)) {
                log.info(
                        "Mark push token as invalid. type={}, messagingErrorCode={}, message={}",
                        messageItem.type(),
                        exception.getMessagingErrorCode(),
                        exception.getMessage());
                return PushNotificationSendResult.INVALID_TOKEN;
            }
            log.warn("Failed to send push notification. type={}", messageItem.type(), exception);
            return PushNotificationSendResult.FAILED;
        }
    }

    private boolean isInvalidTokenError(FirebaseMessagingException exception) {
        return exception.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED;
    }

    private String toJson(Map<String, String> linkParams) {
        try {
            return JSON_MAPPER.writeValueAsString(linkParams);
        } catch (JsonProcessingException exception) {
            log.warn("Failed to serialize linkParams for push payload", exception);
            return null;
        }
    }
}
