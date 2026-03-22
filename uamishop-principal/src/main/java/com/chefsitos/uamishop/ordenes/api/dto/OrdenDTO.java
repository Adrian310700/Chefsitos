package com.chefsitos.uamishop.ordenes.api.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * DTO inter-módulo: resumen de una orden para consumo externo.
 * NO expone el Aggregate Root Orden ni sus entidades internas.
 */
public record OrdenDTO(
    UUID idOrden,
    String numeroOrden,
    UUID clienteId,
    String estado,
    BigDecimal total,
    String moneda,
    List<ItemOrdenResumen> items) {

  /**
   * DTO interno que representa un ítem de la orden en forma resumida.
   */
  public record ItemOrdenResumen(
      UUID productoId,
      String nombreProducto,
      String sku,
      int cantidad,
      BigDecimal precioUnitario) {
  }

}
