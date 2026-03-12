package _ganzi.codoc.global.ratelimit.impl;

import _ganzi.codoc.global.ratelimit.RateLimitPolicy;
import _ganzi.codoc.global.ratelimit.RateLimitResult;
import _ganzi.codoc.global.ratelimit.RateLimitStore;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.EstimationProbe;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class RedisRateLimitStore implements RateLimitStore {

    private static final String redisKeyPrefix = "ratelimit";

    private final ProxyManager<String> proxyManager;

    @Override
    public RateLimitResult tryConsume(String key, RateLimitPolicy policy) {
        Bucket bucket =
                proxyManager.builder().build(namespacedKey(key), () -> createConfiguration(policy));

        ConsumptionProbe probe =
                bucket.tryConsumeAndReturnRemaining(
                        Math.min(policy.consumeTokenOrDefault(), policy.limit()));
        EstimationProbe resetProbe = bucket.estimateAbilityToConsume(policy.limit());

        return new RateLimitResult(
                probe.isConsumed(),
                probe.getRemainingTokens(),
                resetProbe.getNanosToWaitForRefill(),
                probe.getNanosToWaitForRefill());
    }

    private String namespacedKey(String key) {
        return redisKeyPrefix + ":" + key;
    }

    private BucketConfiguration createConfiguration(RateLimitPolicy policy) {
        Bandwidth bandwidth =
                Bandwidth.builder()
                        .capacity(policy.limit())
                        .refillGreedy(policy.limit(), policy.period())
                        .build();

        return BucketConfiguration.builder().addLimit(bandwidth).build();
    }
}
