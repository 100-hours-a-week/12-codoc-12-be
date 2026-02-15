package _ganzi.codoc.notification.api;

import _ganzi.codoc.auth.domain.AuthUser;
import _ganzi.codoc.global.dto.ApiResponse;
import _ganzi.codoc.notification.dto.NotificationPreferenceResponse;
import _ganzi.codoc.notification.dto.NotificationPreferenceUpdateRequest;
import _ganzi.codoc.notification.service.UserNotificationPreferenceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RequiredArgsConstructor
@RequestMapping("/api/notification-preferences")
@RestController
public class NotificationPreferenceController implements NotificationPreferenceApi {

    private final UserNotificationPreferenceService preferenceService;

    @Override
    @GetMapping
    public ResponseEntity<ApiResponse<NotificationPreferenceResponse>> getPreferences(
            @AuthenticationPrincipal AuthUser authUser) {
        return ResponseEntity.ok(
                ApiResponse.success(preferenceService.getPreferences(authUser.userId())));
    }

    @Override
    @PatchMapping
    public ResponseEntity<Void> updatePreference(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody NotificationPreferenceUpdateRequest request) {
        preferenceService.updatePreference(authUser.userId(), request.type(), request.enabled());
        return ResponseEntity.noContent().build();
    }
}
