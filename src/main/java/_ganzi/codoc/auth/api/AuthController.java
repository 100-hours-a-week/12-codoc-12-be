package _ganzi.codoc.auth.api;

import _ganzi.codoc.auth.service.AuthTokenService;
import _ganzi.codoc.auth.service.dto.TokenPairResponse;
import _ganzi.codoc.global.dto.ApiResponse;
import _ganzi.codoc.global.exception.AuthRequiredException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthTokenService authTokenService;
    private final boolean cookieSecure;

    public AuthController(
            AuthTokenService authTokenService, @Value("${jwt.cookie-secure:true}") boolean cookieSecure) {
        this.authTokenService = authTokenService;
        this.cookieSecure = cookieSecure;
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenPairResponse>> refresh(
            @CookieValue(value = "refresh_token", required = false) String refreshToken,
            HttpServletResponse response) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new AuthRequiredException();
        }
        var tokenPair = authTokenService.refreshTokenPair(refreshToken);

        ResponseCookie cookie =
                ResponseCookie.from("refresh_token", tokenPair.refreshToken())
                        .httpOnly(true)
                        .secure(cookieSecure)
                        .sameSite("Lax")
                        .path("/api/auth")
                        .maxAge(authTokenService.getRefreshTokenTtlSeconds())
                        .build();
        response.addHeader("Set-Cookie", cookie.toString());

        return ResponseEntity.ok(
                ApiResponse.success(
                        new TokenPairResponse(
                                tokenPair.accessToken(), tokenPair.tokenType(), tokenPair.expiresIn())));
    }
}
