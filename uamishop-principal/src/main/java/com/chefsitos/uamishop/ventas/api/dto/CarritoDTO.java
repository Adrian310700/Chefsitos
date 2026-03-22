package com.chefsitos.uamishop.ventas.api.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import com.chefsitos.uamishop.ventas.domain.aggregate.Carrito;

public record CarritoDTO(
    UUID carritoId,
    UUID clienteId,
    List<ItemCarritoDTO> items,
    List<DescuentoDTO> descuentos,
    String estado,
    BigDecimal subtotal,
    BigDecimal total,
    String moneda) {

  public static CarritoDTO from(Carrito carrito) {
    return new CarritoDTO(
        carrito.getCarritoId().getValue(),
        carrito.getClienteId().valor(),
        carrito.getItems().stream().map(ItemCarritoDTO::from).toList(),
        carrito.getDescuentos().stream().map(DescuentoDTO::from).toList(),
        carrito.getEstado().name(),
        carrito.calcularSubtotal().cantidad(),
        carrito.calcularTotal().cantidad(),
        carrito.calcularTotal().moneda());
  }
}
