package _ganzi.codoc.user.api;

import _ganzi.codoc.global.dto.ApiResponse;
import _ganzi.codoc.user.service.AvatarService;
import _ganzi.codoc.user.service.dto.UserAvatarListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/avatars")
public class AvatarController implements AvatarApi {

    private final AvatarService avatarService;

    @Override
    @GetMapping
    public ResponseEntity<ApiResponse<UserAvatarListResponse>> getAvatars() {
        return ResponseEntity.ok(ApiResponse.success(avatarService.getAvatarList()));
    }
}
