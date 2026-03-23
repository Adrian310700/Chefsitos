package com.chefsitos.uamishop.ventas.api.dto;

import java.math.BigDecimal;
import java.util.UUID;



public record ItemCarritoDTO(
    UUID productoId,
    String nombreProducto,
    String sku,
    int cantidad,
    BigDecimal precioUnitario,
    BigDecimal subtotal,
    String moneda) {
}
