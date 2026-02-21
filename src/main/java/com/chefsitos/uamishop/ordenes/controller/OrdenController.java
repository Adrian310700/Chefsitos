package com.chefsitos.uamishop.ordenes.controller;

import com.chefsitos.uamishop.ApiError;
import com.chefsitos.uamishop.ordenes.controller.dto.InfoEnvioRequest;
import com.chefsitos.uamishop.ordenes.controller.dto.OrdenRequest;
import com.chefsitos.uamishop.ordenes.controller.dto.OrdenResponseDTO; // El nuevo DTO
import com.chefsitos.uamishop.ordenes.service.OrdenService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.headers.Header;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/ordenes")
@Tag(name = "Órdenes", description = "Operaciones relacionadas con órdenes")
public class OrdenController {

  private final OrdenService ordenService;

  public OrdenController(OrdenService ordenService) {
    this.ordenService = ordenService;
  }

  // ===============================
  // CREAR ORDEN
  // ===============================
  @Operation(summary = "Crear orden", description = "Permite crear una nueva orden")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "201", description = "Orden creada exitosamente",
      headers = @Header(name = "Location", description = "URI del recurso creado", schema = @Schema(type = "string")),
      content = @Content(schema = @Schema(implementation = OrdenResponseDTO.class))), // Cambiado a DTO
    @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos",
      content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(responseCode = "422", description = "Regla de negocio violada",
      content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @PostMapping
  public ResponseEntity<OrdenResponseDTO> crear(@Valid @RequestBody OrdenRequest request) {
    OrdenResponseDTO response = ordenService.crear(request);
    URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
      .buildAndExpand(response.id()).toUri();
    return ResponseEntity.created(location).body(response);
  }

  // ===============================
  // BUSCAR POR ID
  // ===============================
  @Operation(summary = "Buscar orden por ID")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Orden encontrada",
      content = @Content(schema = @Schema(implementation = OrdenResponseDTO.class))),
    @ApiResponse(responseCode = "404", description = "Orden no encontrada",
      content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @GetMapping("/{id}")
  public ResponseEntity<OrdenResponseDTO> buscarPorId(@PathVariable UUID id) {
    return ResponseEntity.ok(ordenService.buscarYConvertirDTO(id));
  }

  // ===============================
  // CONFIRMAR ORDEN
  // ===============================
  @Operation(summary = "Confirmar orden")
  @PutMapping("/{id}/confirmar")
  public ResponseEntity<OrdenResponseDTO> confirmar(@PathVariable UUID id) {
    return ResponseEntity.ok(ordenService.confirmar(id));
  }

  // ===============================
  // PROCESAR PAGO
  // ===============================
  @Operation(summary = "Procesar pago")
  @PutMapping("/{id}/pago")
  public ResponseEntity<OrdenResponseDTO> procesarPago(@PathVariable UUID id, @RequestParam String referenciaPago) {
    return ResponseEntity.ok(ordenService.procesarPago(id, referenciaPago));
  }

  // ===============================
  // MARCAR ENVIADA
  // ===============================
  @Operation(summary = "Marcar orden como enviada")
  @PutMapping("/{id}/enviada")
  public ResponseEntity<OrdenResponseDTO> marcarEnviada(
    @PathVariable UUID id, @Valid @RequestBody InfoEnvioRequest request) {
    return ResponseEntity.ok(ordenService.marcarEnviada(id, request.numeroGuia(), request.proveedorLogistico()));
  }

  // ===============================
  // CANCELAR ORDEN
  // ===============================
  @Operation(summary = "Cancelar orden")
  @PutMapping("/{id}/cancelar")
  public ResponseEntity<OrdenResponseDTO> cancelar(@PathVariable UUID id, @RequestParam String motivo) {
    return ResponseEntity.ok(ordenService.cancelar(id, motivo));
  }

  // Métodos de estado adicionales
  @Operation(summary = "Marcar en preparación")
  @PutMapping("/{id}/en-proceso")
  public ResponseEntity<OrdenResponseDTO> marcarEnProceso(@PathVariable UUID id) {
    return ResponseEntity.ok(ordenService.marcarEnProceso(id));
  }

  @Operation(summary = "Marcar como entregada")
  @PutMapping("/{id}/entregada")
  public ResponseEntity<OrdenResponseDTO> marcarEntregada(@PathVariable UUID id) {
    return ResponseEntity.ok(ordenService.marcarEntregada(id));
  }
}
