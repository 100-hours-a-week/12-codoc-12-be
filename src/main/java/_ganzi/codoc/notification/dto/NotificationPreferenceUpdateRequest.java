package _ganzi.codoc.notification.dto;

import _ganzi.codoc.notification.enums.NotificationType;
import jakarta.validation.constraints.NotNull;

public record NotificationPreferenceUpdateRequest(
        @NotNull(message = "알림 종류 값이 유효하지 않습니다.") NotificationType type,
        @NotNull(message = "알림 수신 여부 값이 유효하지 않습니다.") Boolean enabled) {}
