package _ganzi.codoc.notification.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;

public record NotificationReadRequest(
        @NotEmpty(message = "알림 ID 목록은 비어 있을 수 없습니다.")
                List<@NotNull(message = "알림 ID는 필수입니다.") @Positive(message = "알림 ID는 양수여야 합니다.") Long>
                        notificationIds) {}
