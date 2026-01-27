package _ganzi.codoc.user.api;

import _ganzi.codoc.global.dto.ApiResponse;
import _ganzi.codoc.user.service.dto.UserAvatarListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Avatar", description = "Avatar catalog endpoints")
public interface AvatarApi {

    @Operation(summary = "Get avatar list")
    ResponseEntity<ApiResponse<UserAvatarListResponse>> getAvatars();
}
