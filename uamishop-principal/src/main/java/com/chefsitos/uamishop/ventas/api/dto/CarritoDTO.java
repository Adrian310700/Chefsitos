package com.chefsitos.uamishop.ventas.api.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CarritoDTO(
    UUID carritoId,
    UUID clienteId,
    List<ItemCarritoDTO> items,
    List<DescuentoDTO> descuentos,
    String estado,
    BigDecimal subtotal,
    BigDecimal total,
    String moneda) {
}
