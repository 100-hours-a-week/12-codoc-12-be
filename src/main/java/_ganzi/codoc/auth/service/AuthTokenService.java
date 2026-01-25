package _ganzi.codoc.auth.service;

import _ganzi.codoc.auth.domain.RefreshToken;
import _ganzi.codoc.auth.jwt.JwtTokenProvider;
import _ganzi.codoc.auth.repository.RefreshTokenRepository;
import _ganzi.codoc.auth.service.dto.TokenPairResponse;
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
        UserStatus status = user.getStatus();
        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), status);
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId(), status);
        Instant expiresAt = jwtTokenProvider.getRefreshTokenExpiry();

        refreshTokenRepository
                .findByUser(user)
                .ifPresentOrElse(
                        token -> token.rotate(refreshToken, expiresAt),
                        () -> refreshTokenRepository.save(RefreshToken.create(user, refreshToken, expiresAt)));

        return new TokenPairResponse(
                accessToken, refreshToken, TOKEN_TYPE_BEARER, jwtTokenProvider.getAccessTokenTtlSeconds());
    }
}
