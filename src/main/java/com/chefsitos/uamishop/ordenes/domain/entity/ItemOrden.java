package com.chefsitos.uamishop.ordenes.domain.entity;

import com.chefsitos.uamishop.ordenes.domain.valueObject.ItemOrdenId;
import com.chefsitos.uamishop.ordenes.domain.valueObject.Money;

public record ItemOrden(
  ItemOrdenId id,
  String productoId,
  String nombreProducto,
  String sku,
  int cantidad,
  Money precioUnitario
) {
  public Money calcularSubtotal() {
    return precioUnitario.multiplicar(cantidad);
  }
}
