package _ganzi.codoc.global.ratelimit;

import io.github.bucket4j.ConsumptionProbe;

public record RateLimitResult(ConsumptionProbe probe, long resetNanos, long retryAfterNanos) {}
