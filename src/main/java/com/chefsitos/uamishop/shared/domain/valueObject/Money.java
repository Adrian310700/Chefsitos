package com.chefsitos.uamishop.shared.domain.valueObject;

import java.math.BigDecimal;
import java.util.Objects;

public record Money(BigDecimal valor, String moneda) {

  public Money {
    Objects.requireNonNull(valor, "El valor no puede ser nulo");
    Objects.requireNonNull(moneda, "La moneda no puede ser nula");
  }

  public static Money zero(String moneda) {
    return new Money(BigDecimal.ZERO, moneda);
  }

  public Money sumar(Money otro) {
    validarMoneda(otro);
    return new Money(this.valor.add(otro.valor), this.moneda);
  }

  public Money restar(Money otro) {
    validarMoneda(otro);
    return new Money(this.valor.subtract(otro.valor), this.moneda);
  }

  public Money multiplicar(BigDecimal cantidad) {
    return new Money(this.valor.multiply(cantidad), this.moneda);
  }

  private void validarMoneda(Money otro) {
    if (!this.moneda.equals(otro.moneda)) {
      throw new IllegalArgumentException("No se pueden operar monedas distintas");
    }
  }

  public boolean esMayorQueCero() {
    return this.valor.compareTo(BigDecimal.ZERO) > 0;
  }
}
