package com.chefsitos.uamishop.shared.domain.valueObject;

import java.math.BigDecimal;
import java.util.Objects;

import jakarta.persistence.Embeddable;

@Embeddable
public record Money(BigDecimal cantidad, String moneda) {

  public Money {
    Objects.requireNonNull(cantidad, "La cantidad no puede ser nula");
    Objects.requireNonNull(moneda, "La moneda no puede ser nula");
  }

  public static Money zero(String moneda) {
    return new Money(BigDecimal.ZERO, moneda);
  }

  // Factory method para crear Money en pesos mexicanos
  public static Money pesos(double monto) {
    return new Money(BigDecimal.valueOf(monto), "MXN");
  }

  // RN-VO-01: No se pueden sumar montos de diferentes monedas
  public Money sumar(Money otro) {
    validarMoneda(otro);
    return new Money(this.cantidad.add(otro.cantidad), this.moneda);
  }

  // RN-VO-02: El resultado de una resta no puede ser negativo
  public Money restar(Money otro) {
    validarMoneda(otro);
    BigDecimal resultado = this.cantidad.subtract(otro.cantidad);
    if (resultado.compareTo(BigDecimal.ZERO) < 0) {
      throw new IllegalArgumentException("El resultado de la resta no puede ser negativo");
    }
    return new Money(resultado, this.moneda);
  }

  public Money multiplicar(BigDecimal factor) {
    return new Money(this.cantidad.multiply(factor), this.moneda);
  }

  private void validarMoneda(Money otro) {
    if (!this.moneda.equals(otro.moneda)) {
      throw new IllegalArgumentException("No se pueden operar monedas distintas");
    }
  }

  public boolean esMayorQueCero() {
    return this.cantidad.compareTo(BigDecimal.ZERO) > 0;
  }
}
