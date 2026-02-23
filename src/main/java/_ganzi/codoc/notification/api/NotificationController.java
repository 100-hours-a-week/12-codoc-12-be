package _ganzi.codoc.notification.api;

import _ganzi.codoc.auth.domain.AuthUser;
import _ganzi.codoc.global.dto.ApiResponse;
import _ganzi.codoc.notification.dto.AttendanceNotificationTestPushRequest;
import _ganzi.codoc.notification.dto.NotificationReadRequest;
import _ganzi.codoc.notification.dto.NotificationResponse;
import _ganzi.codoc.notification.dto.NotificationUnreadStatusResponse;
import _ganzi.codoc.notification.enums.PushNotificationSendResult;
import _ganzi.codoc.notification.service.AttendanceNotificationService;
import _ganzi.codoc.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
@RestController
public class NotificationController implements NotificationApi {

    private final NotificationService notificationService;
    private final AttendanceNotificationService attendanceNotificationService;

    @Override
    @GetMapping
    public ResponseEntity<ApiResponse<NotificationResponse>> getRecentNotifications(
            @AuthenticationPrincipal AuthUser authUser) {
        NotificationResponse response = notificationService.getRecentNotifications(authUser.userId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Override
    @GetMapping("/unread-status")
    public ResponseEntity<ApiResponse<NotificationUnreadStatusResponse>> getUnreadStatus(
            @AuthenticationPrincipal AuthUser authUser) {
        NotificationUnreadStatusResponse response =
                notificationService.getUnreadStatus(authUser.userId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Override
    @PatchMapping("/read-status")
    public ResponseEntity<Void> markUnreadNotificationsAsRead(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody NotificationReadRequest request) {
        notificationService.markAsRead(authUser.userId(), request.notificationIds());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "[TEMP] Send attendance push to a specific FCM token for debugging")
    @PostMapping("/debug/attendance-test-push")
    public ResponseEntity<ApiResponse<PushNotificationSendResult>> sendAttendanceTestPush(
            @Valid @RequestBody AttendanceNotificationTestPushRequest request) {
        PushNotificationSendResult sendResult =
                attendanceNotificationService.sendTestReminderToToken(request.pushToken());
        return ResponseEntity.ok(ApiResponse.success(sendResult));
    }
}
