package com.chefsitos.uamishop.shared;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Agrupa anotaciones individuales reutilizables para documentar errores
 * estándar
 * en la especificación OpenApi (Swagger) de forma granular.
 */
public interface ApiErrors {

  /**
   * Documenta una respuesta HTTP 400 Bad Request.
   * Típicamente indica errores de validación en el payload enviado por el cliente
   * o falta de parámetros.
   */
  @Target({ ElementType.METHOD, ElementType.TYPE })
  @Retention(RetentionPolicy.RUNTIME)
  @ApiResponse(responseCode = "400", description = "Solicitud incorrecta o payload inválido", content = @Content(schema = @Schema(implementation = ApiError.class)))
  @interface BadRequest {
  }

  /**
   * Documenta una respuesta HTTP 401 Unauthorized.
   * Indica que el cliente debe proveer credenciales válidas para obtener la
   * respuesta.
   */
  @Target({ ElementType.METHOD, ElementType.TYPE })
  @Retention(RetentionPolicy.RUNTIME)
  @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(schema = @Schema(implementation = ApiError.class)))
  @interface Unauthorized {
  }

  /**
   * Documenta una respuesta HTTP 403 Forbidden.
   * Indica que, aunque el cliente está autenticado, no tiene los roles/permisos
   * suficientes para acceder al recurso.
   */
  @Target({ ElementType.METHOD, ElementType.TYPE })
  @Retention(RetentionPolicy.RUNTIME)
  @ApiResponse(responseCode = "403", description = "Sin permisos suficientes", content = @Content(schema = @Schema(implementation = ApiError.class)))
  @interface Forbidden {
  }

  /**
   * Documenta una respuesta HTTP 404 Not Found.
   * Indica que el servidor no pudo encontrar el recurso solicitado por ID u otro
   * parámetro de búsqueda.
   */
  @Target({ ElementType.METHOD, ElementType.TYPE })
  @Retention(RetentionPolicy.RUNTIME)
  @ApiResponse(responseCode = "404", description = "Recurso no encontrado", content = @Content(schema = @Schema(implementation = ApiError.class)))
  @interface NotFound {
  }

  /**
   * Documenta una respuesta HTTP 409 Conflict.
   * Indica que la solicitud no pudo ser procesada debido a un conflicto con el
   * estado actual del recurso en el dominio
   * (por ejemplo, intentar confirmar una orden que está cancelada).
   */
  @Target({ ElementType.METHOD, ElementType.TYPE })
  @Retention(RetentionPolicy.RUNTIME)
  @ApiResponse(responseCode = "409", description = "Conflicto con el estado del recurso", content = @Content(schema = @Schema(implementation = ApiError.class)))
  @interface Conflict {
  }

  /**
   * Documenta una respuesta HTTP 422 Unprocessable Entity.
   * Típicamente indica que el payload es sintácticamente correcto, pero incumple
   * o transgrede reglas de negocio del dominio interno.
   */
  @Target({ ElementType.METHOD, ElementType.TYPE })
  @Retention(RetentionPolicy.RUNTIME)
  @ApiResponse(responseCode = "422", description = "Regla de negocio violada", content = @Content(schema = @Schema(implementation = ApiError.class)))
  @interface UnprocessableEntity {
  }

  /**
   * Documenta una respuesta HTTP 500 Internal Server Error.
   * Excepciones no controladas o fallos inesperados imprevistos en el backend.
   */
  @Target({ ElementType.METHOD, ElementType.TYPE })
  @Retention(RetentionPolicy.RUNTIME)
  @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content(schema = @Schema(implementation = ApiError.class)))
  @interface ServerError {
  }

  /**
   * Agrupa errores globales críticos que toda la aplicación o todos los endpoints
   * podrían llegar a lanzar estáticamente bajo cualquier circunstancia (500, 401,
   * 403).
   * Se recomienda aplicar a nivel de clase (@Controller).
   */
  @Target({ ElementType.METHOD, ElementType.TYPE })
  @Retention(RetentionPolicy.RUNTIME)
  @Unauthorized
  @Forbidden
  @ServerError
  @interface GlobalErrorResponses {
  }
}
