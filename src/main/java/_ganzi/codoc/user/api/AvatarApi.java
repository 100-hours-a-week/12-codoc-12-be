package _ganzi.codoc.user.api;

import _ganzi.codoc.global.api.docs.ErrorCodes;
import _ganzi.codoc.global.dto.ApiResponse;
import _ganzi.codoc.global.exception.GlobalErrorCode;
import _ganzi.codoc.user.service.dto.UserAvatarListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Avatar", description = "Avatar catalog endpoints")
public interface AvatarApi {

    @Operation(summary = "Get avatar list")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "401",
                description = "AUTH_REQUIRED, UNAUTHORIZED"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "403",
                description = "FORBIDDEN"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "500",
                description = "INTERNAL_SERVER_ERROR")
    })
    @ErrorCodes(
            global = {
                GlobalErrorCode.AUTH_REQUIRED,
                GlobalErrorCode.UNAUTHORIZED,
                GlobalErrorCode.FORBIDDEN,
                GlobalErrorCode.INTERNAL_SERVER_ERROR
            })
    ResponseEntity<ApiResponse<UserAvatarListResponse>> getAvatars();
}
