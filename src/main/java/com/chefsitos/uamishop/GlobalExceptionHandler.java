package com.chefsitos.uamishop;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.chefsitos.uamishop.shared.ApiError;
import com.chefsitos.uamishop.shared.exception.BadRequestException;
import com.chefsitos.uamishop.shared.exception.BusinessRuleException;
import com.chefsitos.uamishop.shared.exception.ConflictException;
import com.chefsitos.uamishop.shared.exception.ForbiddenException;
import com.chefsitos.uamishop.shared.exception.ResourceNotFoundException;
import com.chefsitos.uamishop.shared.exception.UnauthorizedException;

import jakarta.persistence.EntityNotFoundException;

/**
 * Manejador global de excepciones para la API REST.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  /**
   * 404 - Recurso no encontrado
   */
  @ExceptionHandler({ ResourceNotFoundException.class, EntityNotFoundException.class })
  public ResponseEntity<ApiError> handleResourceNotFoundException(
      Exception ex, WebRequest request) {

    log.warn("Recurso no encontrado: {}", ex.getMessage());

    ApiError apiError = new ApiError(
        HttpStatus.NOT_FOUND.value(),
        "Not Found",
        ex.getMessage(),
        getPath(request));

    return new ResponseEntity<>(apiError, HttpStatus.NOT_FOUND);
  }

  /**
   * Maneja reglas de negocio violadas.
   * 422 - Unprocessable Entity
   */
  @ExceptionHandler(BusinessRuleException.class)
  public ResponseEntity<ApiError> handleBusinessRuleException(
      BusinessRuleException ex, WebRequest request) {

    log.warn("Regla de negocio violada: {}", ex.getMessage(), ex.getRule());

    ApiError apiError = new ApiError(
        HttpStatus.UNPROCESSABLE_CONTENT.value(),
        "Unprocessable Entity",
        ex.getMessage(),
        getPath(request));

    return new ResponseEntity<>(apiError, HttpStatus.UNPROCESSABLE_CONTENT);
  }

  /**
   * 400 - Errores de validación (@Valid) y peticiones mal formadas.
   */
  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {

    String defaultMessage = ex.getBindingResult().getFieldErrors().stream()
        .findFirst()
        .map(error -> error.getField() + ": " + error.getDefaultMessage())
        .orElse("Solicitud inválida");

    log.warn("Error de validación: {}", defaultMessage);

    ApiError apiError = new ApiError(
        HttpStatus.BAD_REQUEST.value(),
        "Bad Request",
        defaultMessage,
        getPath(request));

    return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
  }

  /**
   * 422 - Argumentos inválidos lanzados por el dominio (reglas de Value Objects).
   * Ejemplo: país distinto a "México", código postal inválido, teléfono inválido.
   */
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ApiError> handleIllegalArgumentException(
      IllegalArgumentException ex, WebRequest request) {

    log.warn("Argumento inválido (regla de dominio): {}", ex.getMessage());

    ApiError apiError = new ApiError(
        HttpStatus.UNPROCESSABLE_CONTENT.value(),
        "Unprocessable Entity",
        ex.getMessage(),
        getPath(request));

    return new ResponseEntity<>(apiError, HttpStatus.UNPROCESSABLE_CONTENT);
  }

  /**
   * 400 - Petición inválida
   */
  @ExceptionHandler(BadRequestException.class)
  public ResponseEntity<ApiError> handleBadRequestException(
      BadRequestException ex, WebRequest request) {

    log.warn("Petición inválida: {}", ex.getMessage());

    ApiError apiError = new ApiError(
        HttpStatus.BAD_REQUEST.value(),
        "Bad Request",
        ex.getMessage(),
        getPath(request));

    return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
  }

  /**
   * 401 - No autorizado
   */
  @ExceptionHandler(UnauthorizedException.class)
  public ResponseEntity<ApiError> handleUnauthorizedException(
      UnauthorizedException ex, WebRequest request) {

    log.warn("No autorizado: {}", ex.getMessage());

    ApiError apiError = new ApiError(
        HttpStatus.UNAUTHORIZED.value(),
        "Unauthorized",
        ex.getMessage(),
        getPath(request));

    return new ResponseEntity<>(apiError, HttpStatus.UNAUTHORIZED);
  }

  /**
   * 403 - Prohibido
   */
  @ExceptionHandler(ForbiddenException.class)
  public ResponseEntity<ApiError> handleForbiddenException(
      ForbiddenException ex, WebRequest request) {

    log.warn("Acceso prohibido: {}", ex.getMessage());

    ApiError apiError = new ApiError(
        HttpStatus.FORBIDDEN.value(),
        "Forbidden",
        ex.getMessage(),
        getPath(request));

    return new ResponseEntity<>(apiError, HttpStatus.FORBIDDEN);
  }

  /**
   * 409 - Conflictos de estado (por ejemplo, intentos de cambiar a un estado
   * inválido).
   */
  @ExceptionHandler({ IllegalStateException.class, ConflictException.class })
  public ResponseEntity<ApiError> handleIllegalStateException(
      Exception ex, WebRequest request) {

    log.warn("Conflicto de estado: {}", ex.getMessage());

    ApiError apiError = new ApiError(
        HttpStatus.CONFLICT.value(),
        "Conflict",
        ex.getMessage(),
        getPath(request));

    return new ResponseEntity<>(apiError, HttpStatus.CONFLICT);
  }

  /**
   * 500 - Cualquier otra excepción no controlada explícitamente.
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiError> handleGenericException(
      Exception ex, WebRequest request) {

    log.error("Error inesperado", ex);

    ApiError apiError = new ApiError(
        HttpStatus.INTERNAL_SERVER_ERROR.value(),
        "Internal Server Error",
        "Ha ocurrido un error inesperado. Inténtalo más tarde.",
        getPath(request));

    return new ResponseEntity<>(apiError, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  /**
   * Extrae el path de la petición del WebRequest.
   */
  private String getPath(WebRequest request) {
    if (request instanceof ServletWebRequest swr) {
      return swr.getRequest().getRequestURI();
    }
    return null;
  }
}
