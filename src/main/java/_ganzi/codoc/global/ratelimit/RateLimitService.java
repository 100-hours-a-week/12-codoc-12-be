package _ganzi.codoc.global.ratelimit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class RateLimitService {

    private final RateLimitStore rateLimitStore;

    public RateLimitResult tryConsume(String key, RateLimitPolicy policy) {
        RateLimitResult result = rateLimitStore.tryConsume(key, policy);

        if (!result.consumed()) {
            log.warn("Rate Limit Exceeded - key={}", key);
        }

        return result;
    }
}
