package _ganzi.codoc.global.ratelimit;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.EstimationProbe;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class RateLimitService {

    private final RateLimitStore rateLimitStore;

    public RateLimitResult tryConsume(String key, RateLimitPolicy policy) {
        Bucket bucket = rateLimitStore.getBucket(key, policy);
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(policy.consumeTokenOrDefault());
        EstimationProbe resetProbe = bucket.estimateAbilityToConsume(policy.limit());

        if (!probe.isConsumed()) {
            log.warn("Rate Limit Exceeded - key={}", key);
        }

        return new RateLimitResult(
                probe, resetProbe.getNanosToWaitForRefill(), probe.getNanosToWaitForRefill());
    }
}
