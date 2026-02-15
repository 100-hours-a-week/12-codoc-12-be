package _ganzi.codoc.notification.dto;

import _ganzi.codoc.notification.enums.DevicePlatform;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record NotificationDeviceRegisterRequest(
        @NotNull(message = "디바이스 플랫폼 값이 유효하지 않습니다.") DevicePlatform platform,
        @NotBlank(message = "푸시 토큰은 비어 있을 수 없습니다.")
                @Size(max = 512, message = "푸시 토큰 길이는 512자를 초과할 수 없습니다.")
                String pushToken) {}
