package com.chefsitos.uamishop.ventas.api.dto;

import java.math.BigDecimal;

public record DescuentoDTO(
    String codigo,
    String tipo,
    BigDecimal valor,
    BigDecimal montoDescontado,
    String moneda) {
}
