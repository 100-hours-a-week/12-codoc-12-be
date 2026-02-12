package _ganzi.codoc.global.api;

import _ganzi.codoc.global.dto.ApiResponse;
import _ganzi.codoc.global.exception.BaseException;
import _ganzi.codoc.global.exception.GlobalErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<?> handleBaseException(
            BaseException exception, HttpServletRequest request) {
        if (isSseRequest(request)) {
            return ResponseEntity.status(exception.getErrorCode().status()).build();
        }
        return ResponseEntity.status(exception.getErrorCode().status())
                .body(ApiResponse.error(exception.getErrorCode().code(), exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException exception, HttpServletRequest request) {
        return invalidInputResponse(
                request, toFieldErrors(exception.getBindingResult().getFieldErrors()));
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<?> handleBindException(
            BindException exception, HttpServletRequest request) {
        return invalidInputResponse(
                request, toFieldErrors(exception.getBindingResult().getFieldErrors()));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<?> handleConstraintViolationException(
            ConstraintViolationException exception, HttpServletRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();
        exception
                .getConstraintViolations()
                .forEach(
                        violation ->
                                errors.put(violation.getPropertyPath().toString(), violation.getMessage()));

        return invalidInputResponse(request, errors);
    }

    @ExceptionHandler({
        MethodArgumentTypeMismatchException.class,
        HttpMessageNotReadableException.class
    })
    public ResponseEntity<?> handleBadRequestException(
            Exception exception, HttpServletRequest request) {
        return invalidInputResponse(request);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<?> handleMethodNotAllowedException(
            Exception exception, HttpServletRequest request) {
        if (isSseRequest(request)) {
            return ResponseEntity.status(GlobalErrorCode.METHOD_NOT_ALLOWED.status()).build();
        }
        return ResponseEntity.status(GlobalErrorCode.METHOD_NOT_ALLOWED.status())
                .body(
                        ApiResponse.error(
                                GlobalErrorCode.METHOD_NOT_ALLOWED.code(),
                                GlobalErrorCode.METHOD_NOT_ALLOWED.message()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception exception, HttpServletRequest request) {
        GlobalErrorCode errorCode = GlobalErrorCode.INTERNAL_SERVER_ERROR;
        log.error("[{}] {}", errorCode.code(), errorCode.message(), exception);
        if (isSseRequest(request)) {
            return ResponseEntity.status(errorCode.status()).build();
        }
        return ResponseEntity.status(errorCode.status())
                .body(ApiResponse.error(errorCode.code(), errorCode.message()));
    }

    private Map<String, String> toFieldErrors(Iterable<FieldError> fieldErrors) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError error : fieldErrors) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        return errors;
    }

    private ResponseEntity<?> invalidInputResponse(
            HttpServletRequest request, Map<String, String> errors) {
        if (isSseRequest(request)) {
            return ResponseEntity.status(GlobalErrorCode.INVALID_INPUT.status()).build();
        }
        return ResponseEntity.status(GlobalErrorCode.INVALID_INPUT.status())
                .body(
                        ApiResponse.error(
                                GlobalErrorCode.INVALID_INPUT.code(),
                                GlobalErrorCode.INVALID_INPUT.message(),
                                errors));
    }

    private ResponseEntity<?> invalidInputResponse(HttpServletRequest request) {
        if (isSseRequest(request)) {
            return ResponseEntity.status(GlobalErrorCode.INVALID_INPUT.status()).build();
        }
        return ResponseEntity.status(GlobalErrorCode.INVALID_INPUT.status())
                .body(
                        ApiResponse.error(
                                GlobalErrorCode.INVALID_INPUT.code(), GlobalErrorCode.INVALID_INPUT.message()));
    }

    private boolean isSseRequest(HttpServletRequest request) {
        String accept = request.getHeader("Accept");
        return accept != null && accept.contains("text/event-stream");
    }
}
