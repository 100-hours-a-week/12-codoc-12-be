package _ganzi.codoc.global.ratelimit;

import _ganzi.codoc.auth.domain.AuthUser;
import _ganzi.codoc.auth.support.AuthUserResolver;
import _ganzi.codoc.global.exception.RateLimitExceededException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private static final long NANOS_PER_SECOND = java.util.concurrent.TimeUnit.SECONDS.toNanos(1);

    private final RateLimitService rateLimitService;
    private final AuthUserResolver authUserResolver;
    private final Map<RateLimitApiType, RateLimitPolicy> policyMap;

    public RateLimitInterceptor(
            RateLimitService rateLimitService,
            AuthUserResolver authUserResolver,
            RateLimitProperties rateLimitProperties) {
        this.rateLimitService = rateLimitService;
        this.authUserResolver = authUserResolver;
        this.policyMap =
                Map.of(
                        RateLimitApiType.CHATBOT_STREAM,
                        RateLimitPolicy.from(rateLimitProperties.chatbotStream()),
                        RateLimitApiType.GLOBAL,
                        RateLimitPolicy.from(rateLimitProperties.global()));
    }

    @Override
    public boolean preHandle(
            HttpServletRequest request, HttpServletResponse response, Object handler) {

        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        AuthUser authUser = authUserResolver.resolveAuthUser();
        String prefix =
                authUser == null ? "ip:" + clientIp(request) : "userId:" + authUser.getUsername();
        RateLimitApiType apiType = resolveApiType(handlerMethod);
        applyRateLimit(prefix, policyMap.get(RateLimitApiType.GLOBAL), response);

        if (apiType == RateLimitApiType.CHATBOT_STREAM) {
            String detailKey = prefix + ":" + apiType;
            applyRateLimit(detailKey, policyMap.get(apiType), response);
        }
        return true;
    }

    private String clientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            int comma = xff.indexOf(',');
            return (comma > 0) ? xff.substring(0, comma).trim() : xff.trim();
        }
        return request.getRemoteAddr();
    }

    private RateLimitApiType resolveApiType(HandlerMethod handlerMethod) {

        RateLimit rateLimit = handlerMethod.getMethodAnnotation(RateLimit.class);

        if (rateLimit == null) {
            rateLimit = handlerMethod.getBeanType().getAnnotation(RateLimit.class);
        }

        return (rateLimit != null) ? rateLimit.type() : RateLimitApiType.GLOBAL;
    }

    private void applyRateLimit(String key, RateLimitPolicy policy, HttpServletResponse response) {
        if (policy == null || Boolean.FALSE.equals(policy.enabled())) {
            return;
        }

        RateLimitResult result = rateLimitService.tryConsume(key, policy);
        applyHeaders(response, policy, result);

        if (!result.probe().isConsumed()) {
            throw new RateLimitExceededException();
        }
    }

    private void applyHeaders(
            HttpServletResponse response, RateLimitPolicy policy, RateLimitResult result) {
        response.setHeader("RateLimit-Limit", String.valueOf(policy.limit()));
        response.setHeader("RateLimit-Remaining", String.valueOf(result.probe().getRemainingTokens()));
        response.setHeader("RateLimit-Reset", String.valueOf(toCeilSeconds(result.resetNanos())));

        if (!result.probe().isConsumed()) {
            response.setHeader("Retry-After", String.valueOf(toCeilSeconds(result.retryAfterNanos())));
        }
    }

    private long toCeilSeconds(long nanos) {
        return Math.max(0, (nanos + NANOS_PER_SECOND - 1) / NANOS_PER_SECOND);
    }
}
