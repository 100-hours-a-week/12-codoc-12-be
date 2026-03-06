package _ganzi.codoc.auth.config;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.web.util.matcher.IpAddressMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

public class ActuatorCidrRequestMatcher implements RequestMatcher {

    private final List<IpAddressMatcher> cidrMatchers;

    public ActuatorCidrRequestMatcher(String cidrsCsv) {
        this.cidrMatchers =
                Arrays.stream(cidrsCsv.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .map(IpAddressMatcher::new)
                        .collect(Collectors.toList());
    }

    @Override
    public boolean matches(HttpServletRequest request) {
        String clientIp = extractClientIp(request);
        for (IpAddressMatcher matcher : cidrMatchers) {
            if (matcher.matches(clientIp)) {
                return true;
            }
        }
        return false;
    }

    private String extractClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            int comma = xff.indexOf(',');
            return (comma > 0) ? xff.substring(0, comma).trim() : xff.trim();
        }
        return request.getRemoteAddr();
    }
}
