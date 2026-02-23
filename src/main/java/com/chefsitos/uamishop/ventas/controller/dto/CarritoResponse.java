package com.chefsitos.uamishop.ventas.controller.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import com.chefsitos.uamishop.ventas.domain.aggregate.Carrito;

public record CarritoResponse(
    UUID carritoId,
    UUID clienteId,
    List<ItemCarritoResponse> items,
    List<DescuentoResponse> descuentos,
    String estado,
    BigDecimal subtotal,
    BigDecimal total,
    String moneda) {

  public static CarritoResponse from(Carrito carrito) {
    return new CarritoResponse(
        carrito.getCarritoId().getValue(),
        carrito.getClienteId().valor(),
        carrito.getItems().stream().map(ItemCarritoResponse::from).toList(),
        carrito.getDescuentos().stream().map(DescuentoResponse::from).toList(),
        carrito.getEstado().name(),
        carrito.calcularSubtotal().cantidad(),
        carrito.calcularTotal().cantidad(),
        carrito.calcularTotal().moneda());
  }
}
