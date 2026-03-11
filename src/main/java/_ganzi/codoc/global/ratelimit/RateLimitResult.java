package _ganzi.codoc.global.ratelimit;

public record RateLimitResult(
        boolean consumed, long remainingTokens, long resetNanos, long retryAfterNanos) {}
