package _ganzi.codoc.auth.api;

import _ganzi.codoc.auth.service.dto.TokenPairResponse;
import _ganzi.codoc.global.api.docs.ErrorCodes;
import _ganzi.codoc.global.dto.ApiResponse;
import _ganzi.codoc.global.exception.GlobalErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;

@Tag(name = "Auth", description = "Authentication endpoints")
public interface AuthApi {

    @Operation(summary = "Refresh access token")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "401",
                description = "AUTH_REQUIRED"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "500",
                description = "INTERNAL_SERVER_ERROR")
    })
    @ErrorCodes(global = {GlobalErrorCode.AUTH_REQUIRED, GlobalErrorCode.INTERNAL_SERVER_ERROR})
    ResponseEntity<ApiResponse<TokenPairResponse>> refresh(
            String refreshToken, HttpServletResponse response);

    @Operation(summary = "Logout")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "401",
                description = "AUTH_REQUIRED"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "500",
                description = "INTERNAL_SERVER_ERROR")
    })
    @ErrorCodes(global = {GlobalErrorCode.AUTH_REQUIRED, GlobalErrorCode.INTERNAL_SERVER_ERROR})
    ResponseEntity<Void> logout(String refreshToken, HttpServletResponse response);

    @Operation(summary = "Kakao authorize redirect")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "500",
                description = "INTERNAL_SERVER_ERROR")
    })
    @ErrorCodes(global = {GlobalErrorCode.INTERNAL_SERVER_ERROR})
    ResponseEntity<Void> kakaoAuthorize(HttpServletResponse response);

    @Operation(summary = "Kakao callback")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "401",
                description = "AUTH_REQUIRED, AUTH_STATE_MISMATCH"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "500",
                description = "INTERNAL_SERVER_ERROR")
    })
    @ErrorCodes(
            global = {
                GlobalErrorCode.AUTH_REQUIRED,
                GlobalErrorCode.AUTH_STATE_MISMATCH,
                GlobalErrorCode.INTERNAL_SERVER_ERROR
            })
    ResponseEntity<Void> kakaoCallback(
            String code,
            String state,
            String error,
            String errorDescription,
            String expectedState,
            HttpServletResponse response);
}
