package _ganzi.codoc.global.ratelimit;

import _ganzi.codoc.auth.domain.AuthUser;
import _ganzi.codoc.auth.support.AuthUserResolver;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

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
        applyGlobalRateLimit(prefix);
        applyDetailRateLimit(prefix, apiType);
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

    private void applyGlobalRateLimit(String prefix) {
        RateLimitPolicy policy = policyMap.get(RateLimitApiType.GLOBAL);
        if (policy == null || Boolean.FALSE.equals(policy.enabled())) {
            return;
        }

        rateLimitService.tryConsume(prefix, policy);
    }

    private void applyDetailRateLimit(String prefix, RateLimitApiType apiType) {
        if (apiType == RateLimitApiType.GLOBAL) {
            return;
        }

        RateLimitPolicy policy = policyMap.get(apiType);
        if (policy == null || Boolean.FALSE.equals(policy.enabled())) {
            return;
        }

        rateLimitService.tryConsume(prefix + ":" + apiType, policy);
    }
}
