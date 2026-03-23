package com.chefsitos.uamishop.ventas.controller.dto;

import java.math.BigDecimal;
import java.util.UUID;

import com.chefsitos.uamishop.ventas.domain.entity.ItemCarrito;

public record ItemCarritoDTO(
    UUID productoId,
    String nombreProducto,
    String sku,
    int cantidad,
    BigDecimal precioUnitario,
    BigDecimal subtotal,
    String moneda) {

  public static ItemCarritoDTO from(ItemCarrito item) {
    return new ItemCarritoDTO(
        item.getProducto().getProductoId().valor(),
        item.getProducto().nombreProducto(),
        item.getProducto().sku(),
        item.getCantidad(),
        item.getPrecioUnitario().cantidad(),
        item.calcularSubtotal().cantidad(),
        item.getPrecioUnitario().moneda());
  }
}
