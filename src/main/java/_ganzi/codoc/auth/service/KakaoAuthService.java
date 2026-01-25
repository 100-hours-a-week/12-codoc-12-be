package _ganzi.codoc.auth.service;

import _ganzi.codoc.auth.domain.SocialLogin;
import _ganzi.codoc.auth.enums.SocialProvider;
import _ganzi.codoc.auth.repository.SocialLoginRepository;
import _ganzi.codoc.auth.service.AuthTokenService.TokenPair;
import _ganzi.codoc.auth.service.dto.KakaoTokenResponse;
import _ganzi.codoc.auth.service.dto.KakaoUserResponse;
import _ganzi.codoc.global.exception.AuthStateMismatchException;
import _ganzi.codoc.user.domain.User;
import _ganzi.codoc.user.enums.UserStatus;
import _ganzi.codoc.user.service.UserService;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class KakaoAuthService {

    private static final int STATE_TTL_SECONDS = 300;

    private final KakaoOAuthClient kakaoOAuthClient;
    private final SocialLoginRepository socialLoginRepository;
    private final UserService userService;
    private final AuthTokenService authTokenService;

    @Value("${kakao.scope:}")
    private String scope;

    public String generateState() {
        return UUID.randomUUID().toString();
    }

    public int getStateTtlSeconds() {
        return STATE_TTL_SECONDS;
    }

    public String buildAuthorizeUrl(String state, String clientId, String redirectUri) {
        StringBuilder url =
                new StringBuilder("https://kauth.kakao.com/oauth/authorize")
                        .append("?response_type=code")
                        .append("&client_id=")
                        .append(clientId)
                        .append("&redirect_uri=")
                        .append(redirectUri)
                        .append("&state=")
                        .append(state);
        if (!scope.isBlank()) {
            url.append("&scope=").append(URLEncoder.encode(scope, StandardCharsets.UTF_8));
        }
        return url.toString();
    }

    @Transactional
    public TokenPair handleCallback(String code, String state, String expectedState) {
        if (expectedState == null || !expectedState.equals(state)) {
            throw new AuthStateMismatchException();
        }
        KakaoTokenResponse tokenResponse = kakaoOAuthClient.exchangeToken(code);
        KakaoUserResponse userResponse = kakaoOAuthClient.fetchUser(tokenResponse.accessToken());

        SocialLogin socialLogin =
                socialLoginRepository
                        .findByProviderNameAndProviderUserId(SocialProvider.KAKAO, userResponse.id())
                        .orElseGet(() -> createSocialLogin(userResponse));

        User user = socialLogin.getUser();
        if (user.getStatus() == UserStatus.DORMANT) {
            user.reviveFromDormant();
        }

        return authTokenService.issueTokenPairInternal(user);
    }

    private SocialLogin createSocialLogin(KakaoUserResponse userResponse) {
        User user = userService.createOnboardingUser();
        return socialLoginRepository.save(
                SocialLogin.create(user, SocialProvider.KAKAO, userResponse.id()));
    }
}
