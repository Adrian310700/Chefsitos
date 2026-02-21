package com.chefsitos.uamishop;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  // 404 - Not Found
  @ExceptionHandler(IllegalArgumentException.class) // Usado cuando no se encuentra un ID
  public ResponseEntity<ApiError> handleNotFound(IllegalArgumentException ex, WebRequest request) {
    return buildResponse(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), request);
  }

  // 422 - Unprocessable Entity (Reglas de Negocio)
  @ExceptionHandler(BusinessRuleException.class)
  public ResponseEntity<ApiError> handleBusinessRule(BusinessRuleException ex, WebRequest request) {
    return buildResponse(HttpStatus.UNPROCESSABLE_ENTITY, "Unprocessable Entity", ex.getMessage(), request);
  }

  // 409 - Conflict (Estado inválido de la orden)
  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<ApiError> handleConflict(IllegalStateException ex, WebRequest request) {
    return buildResponse(HttpStatus.CONFLICT, "Conflict", ex.getMessage(), request);
  }

  // 500 - Error
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiError> handleAll(Exception ex, WebRequest request) {
    log.error("Error no controlado: ", ex);
    return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "Ocurrió un error inesperado", request);
  }

  private ResponseEntity<ApiError> buildResponse(HttpStatus status, String error, String message, WebRequest request) {
    log.warn("{}: {}", error, message);
    ApiError apiError = new ApiError(status.value(), error, message, getPath(request));
    return new ResponseEntity<>(apiError, status);
  }

  private String getPath(WebRequest request) {
    if (request instanceof ServletWebRequest) {
      return ((ServletWebRequest) request).getRequest().getRequestURI();
    }
    return null;
  }
}
