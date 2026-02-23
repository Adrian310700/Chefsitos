package com.chefsitos.uamishop.ventas.controller.dto;

import java.math.BigDecimal;

import com.chefsitos.uamishop.ventas.domain.valueObject.DescuentoAplicado;

public record DescuentoResponse(
    String codigo,
    String tipo,
    BigDecimal valor,
    BigDecimal montoDescontado,
    String moneda) {

  public static DescuentoResponse from(DescuentoAplicado descuento) {
    BigDecimal monto = null;
    String moneda = null;
    // montoDescontado puede ser null si el descuento aún no fue aplicado sobre un
    // subtotal
    if (descuento.montoDescontado() != null) {
      monto = descuento.montoDescontado().cantidad();
      moneda = descuento.montoDescontado().moneda();
    }

    return new DescuentoResponse(
        descuento.codigo(),
        descuento.tipo().name(),
        descuento.valor(),
        monto,
        moneda);
  }
}
