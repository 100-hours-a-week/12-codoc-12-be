package _ganzi.codoc.global.ratelimit;

import java.time.Duration;
import lombok.Builder;

@Builder
public record RateLimitPolicy(
        Boolean enabled, Integer limit, Duration period, Integer consumeToken) {

    public static RateLimitPolicy from(RateLimitProperties.Policy policy) {
        return RateLimitPolicy.builder()
                .enabled(policy.enabled())
                .limit(policy.limit())
                .period(policy.period())
                .consumeToken(policy.consumeToken())
                .build();
    }

    public int consumeTokenOrDefault() {
        return consumeToken != null && consumeToken > 0 ? consumeToken : 1;
    }
}
