package _ganzi.codoc.global.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class LoadtestMdcFilter extends OncePerRequestFilter {

    private static final String LOADTEST_HEADER = "X-Loadtest";
    private static final String MDC_KEY = "loadtest";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String headerValue = request.getHeader(LOADTEST_HEADER);
        if (headerValue != null && !headerValue.isBlank()) {
            MDC.put(MDC_KEY, headerValue);
        }
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_KEY);
        }
    }
}
