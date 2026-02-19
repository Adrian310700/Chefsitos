package com.chefsitos.uamishop.ordenes.controller;

import com.chefsitos.uamishop.ordenes.domain.aggregate.Orden;
import com.chefsitos.uamishop.ordenes.domain.valueObject.InfoEnvio;
import com.chefsitos.uamishop.ordenes.controller.dto.OrdenRequest;
import com.chefsitos.uamishop.ordenes.service.OrdenService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controller REST del Bounded Context Órdenes.
 * Solo expone endpoints HTTP.
 * No contiene lógica de negocio.
 */
@RestController
@RequestMapping("/api/ordenes")
public class OrdenController {

  private final OrdenService ordenService;

  public OrdenController(OrdenService ordenService) {
    this.ordenService = ordenService;
  }

  /**
   * Crear una nueva orden.
   */
  @PostMapping
  public ResponseEntity<Orden> crear(@RequestBody OrdenRequest request) {

    Orden orden = ordenService.crear(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(orden); // respuesta al cliente
  }

  /**
   * Buscar orden por ID.
   */
  @GetMapping("/{id}")
  public ResponseEntity<Orden> buscarPorId(@PathVariable UUID id) {
    return ResponseEntity.ok(ordenService.buscarPorId(id));// devuelve status 200
  }

  /**
   * Listar todas las órdenes.
   */
  @GetMapping
  public ResponseEntity<List<Orden>> buscarTodas() {
    return ResponseEntity.ok(ordenService.buscarTodas());
  }

  /**
   * Confirmar orden.
   */
  @PutMapping("/{id}/confirmar")
  public ResponseEntity<Orden> confirmar(@PathVariable UUID id) {
    return ResponseEntity.ok(ordenService.confirmar(id));
  }

  /**
   * Procesar pago.
   */
  @PutMapping("/{id}/pago")
  public ResponseEntity<Orden> procesarPago(
      @PathVariable UUID id,
      @RequestParam String referenciaPago) {

    return ResponseEntity.ok(
        ordenService.procesarPago(id, referenciaPago));
  }

  /**
   * Marcar en proceso.
   */
  @PutMapping("/{id}/en-proceso")
  public ResponseEntity<Orden> marcarEnProceso(@PathVariable UUID id) {
    return ResponseEntity.ok(ordenService.marcarEnProceso(id));
  }

  /**
   * Marcar como enviada.
   */
  @PutMapping("/{id}/enviada")
  public ResponseEntity<Orden> marcarEnviada(
      @PathVariable UUID id,
      @RequestBody InfoEnvio infoEnvio) {

    return ResponseEntity.ok(
        ordenService.marcarEnviada(id, infoEnvio));
  }

  /**
   * Marcar como en tránsito.
   */
  @PutMapping("/{id}/en-transito")
  public ResponseEntity<Orden> marcarEnTransito(@PathVariable UUID id) {
    return ResponseEntity.ok(ordenService.marcarEnTransito(id));
  }

  /**
   * Marcar como entregada.
   */
  @PutMapping("/{id}/entregada")
  public ResponseEntity<Orden> marcarEntregada(@PathVariable UUID id) {
    return ResponseEntity.ok(ordenService.marcarEntregada(id));
  }

  /**
   * Cancelar orden.
   */
  @PutMapping("/{id}/cancelar")
  public ResponseEntity<Orden> cancelar(
      @PathVariable UUID id,
      @RequestParam String motivo) {

    return ResponseEntity.ok(
        ordenService.cancelar(id, motivo));
  }
}
