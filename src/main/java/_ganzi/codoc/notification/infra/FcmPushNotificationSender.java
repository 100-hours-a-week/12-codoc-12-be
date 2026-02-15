package _ganzi.codoc.notification.infra;

import _ganzi.codoc.notification.dto.NotificationMessageItem;
import _ganzi.codoc.notification.service.PushNotificationSender;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
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
    public void send(NotificationMessageItem messageItem, String pushToken) {

        FirebaseMessaging firebaseMessaging = firebaseMessagingProvider.getIfAvailable();
        if (firebaseMessaging == null) {
            log.info("Skip push notification send because FCM is unavailable");
            return;
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
        } catch (FirebaseMessagingException exception) {
            log.warn("Failed to send push notification. type={}", messageItem.type(), exception);
        }
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
