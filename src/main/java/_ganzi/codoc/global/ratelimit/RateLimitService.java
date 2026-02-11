package _ganzi.codoc.global.ratelimit;

import _ganzi.codoc.global.exception.RateLimitExceededException;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class RateLimitService {

    private final RateLimitStore rateLimitStore;

    public void tryConsume(String key, RateLimitPolicy policy) {

        Bucket bucket = rateLimitStore.getBucket(key, policy);
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(policy.consumeToken());

        if (!probe.isConsumed()) {
            log.warn("Rate Limit Exceeded - key={}", key);
            throw new RateLimitExceededException();
        }
    }
}
