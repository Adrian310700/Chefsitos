package com.chefsitos.uamishop.shared;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * Modelo estándar de error para las respuestas de la API.
 */
@Data
public class ApiError {

  private final LocalDateTime timestamp;
  private final int status;
  private final String error;
  private final String message;
  private final String path;

  public ApiError(int status, String error, String message, String path) {
    this.timestamp = LocalDateTime.now();
    this.status = status;
    this.error = error;
    this.message = message;
    this.path = path;
  }
}
