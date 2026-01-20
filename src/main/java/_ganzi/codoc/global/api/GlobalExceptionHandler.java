package _ganzi.codoc.global.api;

import _ganzi.codoc.global.dto.ApiResponse;
import _ganzi.codoc.global.exception.BaseException;
import _ganzi.codoc.global.exception.GlobalErrorCode;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
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
  public ResponseEntity<ApiResponse<Void>> handleBaseException(BaseException exception) {
    return ResponseEntity.status(exception.getErrorCode().status())
        .body(ApiResponse.error(exception.getErrorCode().code(), exception.getMessage()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse<Map<String, String>>> handleMethodArgumentNotValidException(
      MethodArgumentNotValidException exception) {
    Map<String, String> errors = new LinkedHashMap<>();
    for (FieldError error : exception.getBindingResult().getFieldErrors()) {
      errors.put(error.getField(), error.getDefaultMessage());
    }

    return ResponseEntity.status(GlobalErrorCode.INVALID_INPUT.status())
        .body(
            ApiResponse.error(
                GlobalErrorCode.INVALID_INPUT.code(),
                GlobalErrorCode.INVALID_INPUT.message(),
                errors));
  }

  @ExceptionHandler({
    MethodArgumentTypeMismatchException.class,
    HttpMessageNotReadableException.class
  })
  public ResponseEntity<ApiResponse<Void>> handleBadRequestException(Exception exception) {
    return ResponseEntity.status(GlobalErrorCode.INVALID_INPUT.status())
        .body(
            ApiResponse.error(
                GlobalErrorCode.INVALID_INPUT.code(), GlobalErrorCode.INVALID_INPUT.message()));
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<ApiResponse<Void>> handleMethodNotAllowedException(Exception exception) {
    return ResponseEntity.status(GlobalErrorCode.METHOD_NOT_ALLOWED.status())
        .body(
            ApiResponse.error(
                GlobalErrorCode.METHOD_NOT_ALLOWED.code(),
                GlobalErrorCode.METHOD_NOT_ALLOWED.message()));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Void>> handleException(Exception exception) {
    log.error("서버 에러 발생", exception);
    return ResponseEntity.status(GlobalErrorCode.INTERNAL_SERVER_ERROR.status())
        .body(
            ApiResponse.error(
                GlobalErrorCode.INTERNAL_SERVER_ERROR.code(),
                GlobalErrorCode.INTERNAL_SERVER_ERROR.message()));
  }
}
