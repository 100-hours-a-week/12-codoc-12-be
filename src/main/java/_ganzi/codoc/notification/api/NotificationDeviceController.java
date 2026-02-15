package _ganzi.codoc.notification.api;

import _ganzi.codoc.auth.domain.AuthUser;
import _ganzi.codoc.notification.dto.NotificationDeviceRegisterRequest;
import _ganzi.codoc.notification.service.UserDeviceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RequiredArgsConstructor
@RequestMapping("/api/notification-devices")
@RestController
public class NotificationDeviceController implements NotificationDeviceApi {

    private final UserDeviceService userDeviceService;

    @Override
    @PutMapping
    public ResponseEntity<Void> registerDevice(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody NotificationDeviceRegisterRequest request) {
        userDeviceService.registerDevice(authUser.userId(), request.platform(), request.pushToken());
        return ResponseEntity.noContent().build();
    }

    @Override
    @DeleteMapping
    public ResponseEntity<Void> deactivateDevice(@AuthenticationPrincipal AuthUser authUser) {
        userDeviceService.deactivateDevice(authUser.userId());
        return ResponseEntity.noContent().build();
    }
}
