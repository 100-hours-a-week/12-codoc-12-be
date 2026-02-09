package _ganzi.codoc.auth.api;

import _ganzi.codoc.auth.api.dto.DevAuthRequest;
import _ganzi.codoc.auth.api.dto.DevAuthResponse;
import _ganzi.codoc.auth.api.dto.DevRefreshRequest;
import _ganzi.codoc.auth.service.AuthTokenService;
import _ganzi.codoc.auth.service.AuthTokenService.TokenPair;
import _ganzi.codoc.auth.service.DevAuthService;
import _ganzi.codoc.global.dto.ApiResponse;
import _ganzi.codoc.global.exception.AuthRequiredException;
import _ganzi.codoc.user.domain.User;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Profile("dev")
@RestController
@RequestMapping("/api/dev/auth")
@RequiredArgsConstructor
public class DevAuthController {

    private static final long DEFAULT_DEV_ACCESS_TTL_MINUTES = 720;

    private final DevAuthService devAuthService;
    private final AuthTokenService authTokenService;

    @Value("${jwt.cookie-secure:true}")
    private boolean cookieSecure;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<DevAuthResponse>> signup(
            @RequestBody(required = false) DevAuthRequest request) {
        String nickname = request == null ? null : request.nickname();
        Duration accessTtl = resolveAccessTtl(request == null ? null : request.accessTokenTtlMinutes());

        User user = devAuthService.createActiveUser(nickname);
        TokenPair tokenPair = authTokenService.issueTokenPairInternal(user, accessTtl);

        return buildAuthResponse(user, tokenPair);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<DevAuthResponse>> login(
            @RequestBody(required = false) DevAuthRequest request) {
        Long userId = request == null ? null : request.userId();
        String nickname = request == null ? null : request.nickname();
        Duration accessTtl = resolveAccessTtl(request == null ? null : request.accessTokenTtlMinutes());

        User user =
                (userId == null)
                        ? devAuthService.createActiveUser(nickname)
                        : devAuthService.getActiveUser(userId);
        TokenPair tokenPair = authTokenService.issueTokenPairInternal(user, accessTtl);

        return buildAuthResponse(user, tokenPair);
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<DevAuthResponse>> refresh(
            @CookieValue(value = "refresh_token", required = false) String refreshToken,
            @RequestBody(required = false) DevRefreshRequest request) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new AuthRequiredException();
        }
        Duration accessTtl = resolveAccessTtl(request == null ? null : request.accessTokenTtlMinutes());

        TokenPair tokenPair = authTokenService.refreshTokenPair(refreshToken, accessTtl);
        ResponseEntity<ApiResponse<DevAuthResponse>> response = buildAuthResponse(null, tokenPair);
        return response;
    }

    private Duration resolveAccessTtl(Long minutes) {
        long resolved = (minutes == null || minutes <= 0) ? DEFAULT_DEV_ACCESS_TTL_MINUTES : minutes;
        return Duration.ofMinutes(resolved);
    }

    private ResponseEntity<ApiResponse<DevAuthResponse>> buildAuthResponse(
            User user, TokenPair tokenPair) {
        ResponseCookie cookie =
                ResponseCookie.from("refresh_token", tokenPair.refreshToken())
                        .httpOnly(true)
                        .secure(cookieSecure)
                        .sameSite("Lax")
                        .path("/")
                        .maxAge(authTokenService.getRefreshTokenTtlSeconds())
                        .build();

        DevAuthResponse body =
                new DevAuthResponse(
                        user == null ? null : user.getId(),
                        tokenPair.accessToken(),
                        tokenPair.tokenType(),
                        tokenPair.expiresIn());

        return ResponseEntity.ok()
                .header("Set-Cookie", cookie.toString())
                .body(ApiResponse.success(body));
    }
}
