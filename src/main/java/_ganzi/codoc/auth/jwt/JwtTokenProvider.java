package _ganzi.codoc.auth.jwt;

import _ganzi.codoc.auth.domain.AuthUser;
import _ganzi.codoc.user.enums.UserStatus;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final Duration accessTokenTtl;
    private final Duration refreshTokenTtl;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-ttl-minutes}") long accessTokenTtlMinutes,
            @Value("${jwt.refresh-token-ttl-days}") long refreshTokenTtlDays) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.accessTokenTtl = Duration.ofMinutes(accessTokenTtlMinutes);
        this.refreshTokenTtl = Duration.ofDays(refreshTokenTtlDays);
    }

    public String createAccessToken(Long userId, UserStatus status) {
        return buildToken(userId, status, accessTokenTtl);
    }

    public String createRefreshToken(Long userId, UserStatus status) {
        return buildToken(userId, status, refreshTokenTtl);
    }

    public long getAccessTokenTtlSeconds() {
        return accessTokenTtl.toSeconds();
    }

    public Instant getRefreshTokenExpiry() {
        return Instant.now().plus(refreshTokenTtl);
    }

    public long getRefreshTokenTtlSeconds() {
        return refreshTokenTtl.toSeconds();
    }

    public AuthUser parseUser(String token) {
        var claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
        Number userId = claims.get("userId", Number.class);
        String status = claims.get("status", String.class);
        return new AuthUser(userId.longValue(), UserStatus.valueOf(status));
    }

    private String buildToken(Long userId, UserStatus status, Duration ttl) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("userId", userId)
                .claim("status", status.name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(ttl)))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }
}
