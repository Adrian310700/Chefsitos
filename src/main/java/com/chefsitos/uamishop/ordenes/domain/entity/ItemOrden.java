package com.chefsitos.uamishop.ordenes.domain.entity;

import java.math.BigDecimal;

import com.chefsitos.uamishop.ordenes.domain.valueObject.ItemOrdenId;
import com.chefsitos.uamishop.shared.domain.valueObject.Money;

public record ItemOrden(
    ItemOrdenId id,
    String productoId,
    String nombreProducto,
    String sku,
    int cantidad,
    Money precioUnitario) {
  public Money calcularSubtotal() {
    return precioUnitario.multiplicar(BigDecimal.valueOf(cantidad));
  }
}
