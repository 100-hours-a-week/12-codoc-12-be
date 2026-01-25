package _ganzi.codoc.auth.service;

import _ganzi.codoc.auth.domain.RefreshToken;
import _ganzi.codoc.auth.jwt.JwtTokenProvider;
import _ganzi.codoc.auth.repository.RefreshTokenRepository;
import _ganzi.codoc.auth.service.dto.TokenPairResponse;
import _ganzi.codoc.global.exception.AuthRequiredException;
import _ganzi.codoc.user.domain.User;
import _ganzi.codoc.user.enums.UserStatus;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthTokenService {

    private static final String TOKEN_TYPE_BEARER = "Bearer";

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public TokenPairResponse issueTokenPair(User user) {
        TokenPair tokenPair = issueTokenPairInternal(user);
        return new TokenPairResponse(
                tokenPair.accessToken(), tokenPair.tokenType(), tokenPair.expiresIn());
    }

    @Transactional
    public TokenPair issueTokenPairInternal(User user) {
        UserStatus status = user.getStatus();
        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), status);
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId(), status);
        Instant expiresAt = jwtTokenProvider.getRefreshTokenExpiry();

        refreshTokenRepository
                .findByUser(user)
                .ifPresentOrElse(
                        token -> token.rotate(refreshToken, expiresAt),
                        () -> refreshTokenRepository.save(RefreshToken.create(user, refreshToken, expiresAt)));

        return new TokenPair(
                accessToken, refreshToken, TOKEN_TYPE_BEARER, jwtTokenProvider.getAccessTokenTtlSeconds());
    }

    public long getRefreshTokenTtlSeconds() {
        return jwtTokenProvider.getRefreshTokenTtlSeconds();
    }

    @Transactional
    public TokenPair refreshTokenPair(String refreshTokenValue) {
        RefreshToken refreshToken =
                refreshTokenRepository
                        .findByTokenValue(refreshTokenValue)
                        .orElseThrow(AuthRequiredException::new);
        if (refreshToken.getExpiresAt().isBefore(Instant.now())) {
            throw new AuthRequiredException();
        }
        User user = refreshToken.getUser();
        UserStatus status = user.getStatus();

        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), status);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(user.getId(), status);
        Instant expiresAt = jwtTokenProvider.getRefreshTokenExpiry();

        refreshToken.rotate(newRefreshToken, expiresAt);

        return new TokenPair(
                accessToken,
                newRefreshToken,
                TOKEN_TYPE_BEARER,
                jwtTokenProvider.getAccessTokenTtlSeconds());
    }

    public record TokenPair(
            String accessToken, String refreshToken, String tokenType, long expiresIn) {}
}
