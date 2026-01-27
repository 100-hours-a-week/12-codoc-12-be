package _ganzi.codoc.auth.api;

import _ganzi.codoc.auth.service.dto.TokenPairResponse;
import _ganzi.codoc.global.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;

@Tag(name = "Auth", description = "Authentication endpoints")
public interface AuthApi {

    @Operation(summary = "Refresh access token")
    ResponseEntity<ApiResponse<TokenPairResponse>> refresh(
            String refreshToken, HttpServletResponse response);

    @Operation(summary = "Logout")
    ResponseEntity<Void> logout(String refreshToken, HttpServletResponse response);

    @Operation(summary = "Kakao authorize redirect")
    ResponseEntity<Void> kakaoAuthorize(HttpServletResponse response);

    @Operation(summary = "Kakao callback")
    ResponseEntity<Void> kakaoCallback(
            String code,
            String state,
            String error,
            String errorDescription,
            String expectedState,
            HttpServletResponse response);
}
