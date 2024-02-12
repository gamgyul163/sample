package com.dayone.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class CustomExceptionHandler {

  @ExceptionHandler(AbstractException.class)
  protected ResponseEntity<ErrorResponse> handleCustomException(AbstractException e) {
    e.printStackTrace();
    ErrorResponse errorResponse = ErrorResponse.builder()
        .code(e.getStatusCode())
        .message(e.getMessage())
        .build();

    return new ResponseEntity<>(errorResponse, HttpStatus.resolve(e.getStatusCode()));
  }

  @ExceptionHandler(AuthenticationException.class)
  protected ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException e) {
    e.printStackTrace();
    ErrorResponse errorResponse = ErrorResponse.builder()
        .code(HttpStatus.UNAUTHORIZED.value())
        .message(e.getMessage())
        .build();

    return new ResponseEntity<>(errorResponse,
        HttpStatus.resolve(HttpStatus.UNAUTHORIZED.value()));
  }

  @ExceptionHandler(Exception.class)
  protected ResponseEntity<ErrorResponse> handleException(Exception e) {
    e.printStackTrace();
    ErrorResponse errorResponse = ErrorResponse.builder()
        .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
        .message("내부 서버 오류가 발생했습니다.")
        .build();

    return new ResponseEntity<>(errorResponse,
        HttpStatus.resolve(HttpStatus.INTERNAL_SERVER_ERROR.value()));
  }
}
