package _ganzi.codoc.auth.api;

import _ganzi.codoc.auth.service.AuthTokenService;
import _ganzi.codoc.auth.service.KakaoAuthService;
import _ganzi.codoc.auth.service.dto.TokenPairResponse;
import _ganzi.codoc.global.dto.ApiResponse;
import _ganzi.codoc.global.exception.AuthRequiredException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthTokenService authTokenService;
    private final KakaoAuthService kakaoAuthService;
    private final boolean cookieSecure;
    private final String kakaoClientId;
    private final String kakaoRedirectUri;

    public AuthController(
            AuthTokenService authTokenService,
            KakaoAuthService kakaoAuthService,
            @Value("${jwt.cookie-secure:true}") boolean cookieSecure,
            @Value("${kakao.client-id}") String kakaoClientId,
            @Value("${kakao.redirect-uri}") String kakaoRedirectUri) {
        this.authTokenService = authTokenService;
        this.kakaoAuthService = kakaoAuthService;
        this.cookieSecure = cookieSecure;
        this.kakaoClientId = kakaoClientId;
        this.kakaoRedirectUri = kakaoRedirectUri;
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

    @GetMapping("/kakao/authorize")
    public ResponseEntity<Void> kakaoAuthorize(HttpServletResponse response) {
        String state = kakaoAuthService.generateState();
        ResponseCookie stateCookie =
                ResponseCookie.from("kakao_oauth_state", state)
                        .httpOnly(true)
                        .secure(cookieSecure)
                        .sameSite("Lax")
                        .path("/api/auth/kakao")
                        .maxAge(kakaoAuthService.getStateTtlSeconds())
                        .build();
        response.addHeader("Set-Cookie", stateCookie.toString());

        String authorizeUrl =
                kakaoAuthService.buildAuthorizeUrl(state, kakaoClientId, kakaoRedirectUri);
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, authorizeUrl)
                .build();
    }

    @GetMapping("/kakao/callback")
    public ResponseEntity<ApiResponse<TokenPairResponse>> kakaoCallback(
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "state", required = false) String state,
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "error_description", required = false) String errorDescription,
            @CookieValue(value = "kakao_oauth_state", required = false) String expectedState,
            HttpServletResponse response) {
        if (error != null) {
            ResponseCookie stateCleanup =
                    ResponseCookie.from("kakao_oauth_state", "").path("/api/auth/kakao").maxAge(0).build();
            response.addHeader("Set-Cookie", stateCleanup.toString());
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, "/login?error=KAKAO_CANCEL")
                    .build();
        }
        if (code == null || state == null) {
            throw new AuthRequiredException();
        }
        var tokenPair = kakaoAuthService.handleCallback(code, state, expectedState);

        ResponseCookie stateCleanup =
                ResponseCookie.from("kakao_oauth_state", "").path("/api/auth/kakao").maxAge(0).build();
        response.addHeader("Set-Cookie", stateCleanup.toString());

        ResponseCookie refreshCookie =
                ResponseCookie.from("refresh_token", tokenPair.refreshToken())
                        .httpOnly(true)
                        .secure(cookieSecure)
                        .sameSite("Lax")
                        .path("/api/auth")
                        .maxAge(authTokenService.getRefreshTokenTtlSeconds())
                        .build();
        response.addHeader("Set-Cookie", refreshCookie.toString());

        return ResponseEntity.ok(
                ApiResponse.success(
                        new TokenPairResponse(
                                tokenPair.accessToken(), tokenPair.tokenType(), tokenPair.expiresIn())));
    }
}
