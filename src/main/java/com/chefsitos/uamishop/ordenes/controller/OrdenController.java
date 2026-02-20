package com.chefsitos.uamishop.ordenes.controller;

import com.chefsitos.uamishop.ordenes.controller.dto.InfoEnvioRequest;
import com.chefsitos.uamishop.ordenes.controller.dto.OrdenRequest;
import com.chefsitos.uamishop.ordenes.domain.aggregate.Orden;
import com.chefsitos.uamishop.ordenes.service.OrdenService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Controller REST del Bounded Context Órdenes.
 */
@RestController
@RequestMapping("/api/ordenes")
@Tag(name = "Órdenes", description = "Operaciones relacionadas con órdenes")
public class OrdenController {

  private final OrdenService ordenService;

  public OrdenController(OrdenService ordenService) {
    this.ordenService = ordenService;
  }


  // CREAR ORDEN

  @Operation(
    summary = "Crear orden",
    description = "Permite crear una nueva orden en el sistema"
  )
  @ApiResponses(value = {
    @ApiResponse(
      responseCode = "201",
      description = "Orden creada exitosamente",
      content = @Content(schema = @Schema(implementation = Orden.class))
    ),
    @ApiResponse(
      responseCode = "400",
      description = "Error de validación"
    )
  })
  @PostMapping
  public ResponseEntity<Orden> crear(
    @Valid @RequestBody OrdenRequest request) {

    Orden orden = ordenService.crear(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(orden);
  }

  // BUSCAR POR ID
  @Operation(
    summary = "Buscar orden por ID",
    description = "Obtiene una orden a partir de su identificador"
  )
  @ApiResponses(value = {
    @ApiResponse(
      responseCode = "200",
      description = "Orden encontrada",
      content = @Content(schema = @Schema(implementation = Orden.class))
    ),
    @ApiResponse(
      responseCode = "404",
      description = "Orden no encontrada"
    )
  })
  @GetMapping("/{id}")
  public ResponseEntity<Orden> buscarPorId(
    @Parameter(description = "ID de la orden")
    @PathVariable UUID id) {

    return ResponseEntity.ok(ordenService.buscarPorId(id));
  }


  // CONFIRMAR ORDEN
  @Operation(summary = "Confirmar orden")
  @PutMapping("/{id}/confirmar")
  public ResponseEntity<Orden> confirmar(
    @Parameter(description = "ID de la orden")
    @PathVariable UUID id) {

    return ResponseEntity.ok(ordenService.confirmar(id));
  }

  // PROCESAR PAGO

  @Operation(summary = "Procesar pago de una orden")
  @PutMapping("/{id}/pago")
  public ResponseEntity<Orden> procesarPago(
    @Parameter(description = "ID de la orden")
    @PathVariable UUID id,
    @Parameter(description = "Referencia del pago")
    @RequestParam String referenciaPago) {

    return ResponseEntity.ok(
      ordenService.procesarPago(id, referenciaPago));
  }


  @Operation(summary = "Marcar orden en preparación")
  @PutMapping("/{id}/en-proceso")
  public ResponseEntity<Orden> marcarEnProceso(
    @Parameter(description = "ID de la orden")
    @PathVariable UUID id) {

    return ResponseEntity.ok(ordenService.marcarEnProceso(id));
  }


  @Operation(summary = "Marcar orden como enviada")
  @PutMapping("/{id}/enviada")
  public ResponseEntity<Orden> marcarEnviada(
    @Parameter(description = "ID de la orden")
    @PathVariable UUID id,
    @Valid @RequestBody InfoEnvioRequest request) {

    return ResponseEntity.ok(
      ordenService.marcarEnviada(id, request));
  }


  @Operation(summary = "Marcar orden como entregada")
  @PutMapping("/{id}/entregada")
  public ResponseEntity<Orden> marcarEntregada(
    @Parameter(description = "ID de la orden")
    @PathVariable UUID id) {

    return ResponseEntity.ok(ordenService.marcarEntregada(id));
  }


  @Operation(summary = "Cancelar orden")
  @PutMapping("/{id}/cancelar")
  public ResponseEntity<Orden> cancelar(
    @Parameter(description = "ID de la orden")
    @PathVariable UUID id,
    @Parameter(description = "Motivo de cancelación")
    @RequestParam String motivo) {

    return ResponseEntity.ok(
      ordenService.cancelar(id, motivo));
  }
}
