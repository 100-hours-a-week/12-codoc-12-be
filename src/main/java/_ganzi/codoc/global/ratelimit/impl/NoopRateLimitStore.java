package _ganzi.codoc.global.ratelimit.impl;

import _ganzi.codoc.global.ratelimit.RateLimitPolicy;
import _ganzi.codoc.global.ratelimit.RateLimitResult;
import _ganzi.codoc.global.ratelimit.RateLimitStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "app.rate-limit", name = "enabled", havingValue = "false")
public class NoopRateLimitStore implements RateLimitStore {

    @Override
    public RateLimitResult tryConsume(String key, RateLimitPolicy policy) {
        return new RateLimitResult(true, Long.MAX_VALUE, 0L, 0L);
    }
}
