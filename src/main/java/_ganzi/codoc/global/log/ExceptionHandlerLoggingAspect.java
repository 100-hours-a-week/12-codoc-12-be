package _ganzi.codoc.global.log;

import java.lang.reflect.Method;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@Aspect
@Component
public class ExceptionHandlerLoggingAspect {

    private static final Logger errorLog = LoggerFactory.getLogger("ERROR_LOG");
    private static final String MDC_HAS_EXCEPTION = "has_exception";

    @Around("@annotation(org.springframework.web.bind.annotation.ExceptionHandler)")
    public Object logExceptionHandler(ProceedingJoinPoint joinPoint) throws Throwable {

        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        ExceptionHandler eh = method.getAnnotation(ExceptionHandler.class);

        Exception ex = extractException(joinPoint.getArgs());
        if (ex == null) {
            return joinPoint.proceed();
        }

        // Filter의 http_error 중복 방지
        MDC.put(MDC_HAS_EXCEPTION, "1");

        // 사용자가 원하는 error object의 type/stacktrace를 채움
        MDC.put("error.type", ex.getClass().getSimpleName());

        // stacktrace는 logback의 stackTrace provider가 별도로 찍지만,
        // "형식"을 강제하기 위해 error.stacktrace에도 넣어둔다 (길어질 수 있으니 truncate)
        MDC.put("error.stacktrace", truncate(stackTraceToString(ex), 4000));

        try {
            errorLog.error("User request processed", ex);
            return joinPoint.proceed();
        } finally {
            MDC.remove("error.type");
            MDC.remove("error.stacktrace");
        }
    }

    private Exception extractException(Object[] args) {
        if (args == null) return null;
        for (Object arg : args) {
            if (arg instanceof Exception e) return e;
        }
        return null;
    }

    private String stackTraceToString(Exception ex) {
        StringBuilder sb = new StringBuilder();
        sb.append(ex.toString()).append('\n');
        for (StackTraceElement ste : ex.getStackTrace()) {
            sb.append("  at ").append(ste.toString()).append('\n');
            if (sb.length() > 4500) break;
        }
        return sb.toString();
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        if (s.length() <= max) return s;
        return s.substring(0, max);
    }
}
