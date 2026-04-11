package _ganzi.codoc.global.log;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class LoggingContextFilter extends OncePerRequestFilter {

    private static final Logger accessLog = LoggerFactory.getLogger("ACCESS_LOG");
    private static final Logger errorLog = LoggerFactory.getLogger("ERROR_LOG");

    private static final String MDC_HAS_EXCEPTION = "has_exception";

    @Value("${app.access-log.enabled:false}")
    private boolean accessLogEnabled;

    @Value("${app.error-log.enabled:false}")
    private boolean errorLogEnabled;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        long startNs = System.nanoTime();
        String traceId = firstNonBlank(request.getHeader("X-Request-Id"), UUID.randomUUID().toString());
        String service = envOrDefault("SERVICE_NAME", "backend-api");
        String ip = clientIp(request);

        response.setHeader("X-Request-Id", traceId);

        try {
            MDC.put("service", service);
            MDC.put("trace_id", traceId);
            MDC.put("path", request.getRequestURI());
            MDC.put("context.ip", ip);

            filterChain.doFilter(request, response);
        } finally {
            int status = response.getStatus();
            double latencySeconds = (System.nanoTime() - startNs) / 1_000_000_000.0;

            MDC.put("status", String.valueOf(status));
            MDC.put("latency", String.valueOf(latencySeconds));

            if (accessLogEnabled) {
                accessLog.info("User request processed");
            }

            if (errorLogEnabled && status >= 400 && !"1".equals(MDC.get(MDC_HAS_EXCEPTION))) {
                MDC.put("error.type", "HttpStatus" + status);
                MDC.put("error.stacktrace", "");
                errorLog.warn("http_error");
                MDC.remove("error.type");
                MDC.remove("error.stacktrace");
            }

            MDC.clear();
        }
    }

    private String clientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            int comma = xff.indexOf(',');
            return (comma > 0) ? xff.substring(0, comma).trim() : xff.trim();
        }
        return request.getRemoteAddr();
    }

    private String envOrDefault(String key, String def) {
        String v = System.getenv(key);
        return (v == null || v.isBlank()) ? def : v;
    }

    private String firstNonBlank(String... values) {
        if (values == null) return "";
        for (String v : values) {
            if (v != null && !v.isBlank()) return v.trim();
        }
        return "";
    }
}
