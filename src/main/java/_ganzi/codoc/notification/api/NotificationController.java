package _ganzi.codoc.notification.api;

import _ganzi.codoc.auth.domain.AuthUser;
import _ganzi.codoc.global.dto.ApiResponse;
import _ganzi.codoc.notification.dto.NotificationReadRequest;
import _ganzi.codoc.notification.dto.NotificationResponse;
import _ganzi.codoc.notification.dto.NotificationUnreadStatusResponse;
import _ganzi.codoc.notification.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
@RestController
public class NotificationController implements NotificationApi {

    private final NotificationService notificationService;

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
}
