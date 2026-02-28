package com.chefsitos.uamishop.ordenes.api.dto;

import java.math.BigDecimal;
import java.util.UUID;

import com.chefsitos.uamishop.ordenes.domain.aggregate.Orden;

public record OrdenDTO(
  UUID idOrden,
  String numeroOrden,
  UUID clienteId,
  String estado,
  BigDecimal total,
  String moneda) {

  public static OrdenDTO from(Orden orden) {
    return new OrdenDTO(
      orden.getId().valor(),
      orden.getNumeroOrden(),
      orden.getClienteId().valor(),
      orden.getEstado().name(),
      orden.getTotal().cantidad(),
      orden.getTotal().moneda());
  }
}
