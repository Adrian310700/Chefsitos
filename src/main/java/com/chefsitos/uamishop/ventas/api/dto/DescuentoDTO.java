package com.chefsitos.uamishop.ventas.api.dto;

import java.math.BigDecimal;

import com.chefsitos.uamishop.ventas.domain.valueObject.DescuentoAplicado;

public record DescuentoDTO(
    String codigo,
    String tipo,
    BigDecimal valor,
    BigDecimal montoDescontado,
    String moneda) {

  public static DescuentoDTO from(DescuentoAplicado descuento) {
    BigDecimal monto = null;
    String moneda = null;
    if (descuento.montoDescontado() != null) {
      monto = descuento.montoDescontado().cantidad();
      moneda = descuento.montoDescontado().moneda();
    }
    return new DescuentoDTO(
        descuento.codigo(),
        descuento.tipo().name(),
        descuento.valor(),
        monto,
        moneda);
  }
}
