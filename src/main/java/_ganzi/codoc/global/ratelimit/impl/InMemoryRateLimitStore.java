package _ganzi.codoc.global.ratelimit.impl;

import _ganzi.codoc.global.ratelimit.RateLimitPolicy;
import _ganzi.codoc.global.ratelimit.RateLimitStore;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class InMemoryRateLimitStore implements RateLimitStore {

    private final Map<String, Bucket> bucketStore = new ConcurrentHashMap<>();

    @Override
    public Bucket getBucket(String key, RateLimitPolicy policy) {
        Bandwidth bandwidth =
                Bandwidth.builder()
                        .capacity(policy.limit())
                        .refillIntervally(policy.limit(), policy.period())
                        .build();

        return bucketStore.computeIfAbsent(key, k -> Bucket.builder().addLimit(bandwidth).build());
    }
}
