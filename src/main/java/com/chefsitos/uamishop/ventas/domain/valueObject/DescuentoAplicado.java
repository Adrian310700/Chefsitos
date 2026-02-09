package com.chefsitos.uamishop.ventas.domain.valueObject;

import java.math.BigDecimal;

import com.chefsitos.uamishop.shared.domain.valueObject.Money;
import com.chefsitos.uamishop.ventas.domain.TipoDescuento;

/**
 * Representa un descuento aplicado a un carrito de compras
 */
public record DescuentoAplicado(String codigo, TipoDescuento tipo, BigDecimal valor) {

  /**
   * Calcula el monto del descuento basado en el tipo y valor del descuento
   * aplicado al subtotal.
   *
   * @param subtotal El monto total antes de aplicar el descuento.
   * @return El monto del descuento a aplicar, asegurando que no exceda el 30% del
   *         subtotal.
   */

  public DescuentoAplicado {
    if (codigo == null || codigo.isBlank()) {
      throw new IllegalArgumentException("Código de descuento inválido");
    }
    if (tipo == null) {
      throw new IllegalArgumentException("Indique el tipo de descuento");
    }
    if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Valor de descuento inválido");
    }
    if (valor.compareTo(BigDecimal.valueOf(30)) > 0) {
      throw new IllegalArgumentException("El descuento no puede exceder el 30%");
    }
  }

  public Money calcularDescuento(Money subtotal) {
    if (!subtotal.esMayorQueCero()) {
      return Money.zero(subtotal.moneda());
    }

    BigDecimal porcentajeDecimal = valor.divide(BigDecimal.valueOf(100));
    Money descuento = subtotal.multiplicar(porcentajeDecimal);
    return descuento;
  }

}
