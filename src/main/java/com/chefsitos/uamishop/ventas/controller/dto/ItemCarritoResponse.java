package com.chefsitos.uamishop.ventas.controller.dto;

import java.math.BigDecimal;
import java.util.UUID;

import com.chefsitos.uamishop.ventas.domain.entity.ItemCarrito;

public record ItemCarritoResponse(
    int cantidad,
    UUID productoId,
    String nombreProducto,
    BigDecimal precioUnitario,
    BigDecimal subtotal) {

  public static ItemCarritoResponse from(ItemCarrito itemCarrito) {
    return new ItemCarritoResponse(
        itemCarrito.getCantidad(),
        itemCarrito.getProducto().getProductoId().valor(),
        itemCarrito.getProducto().nombreProducto(),
        itemCarrito.getPrecioUnitario().cantidad(),
        itemCarrito.calcularSubtotal().cantidad());
  }
}
