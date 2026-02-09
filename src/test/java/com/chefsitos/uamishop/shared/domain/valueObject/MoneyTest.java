package com.chefsitos.uamishop.shared.domain.valueObject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Dominio: Value Object - Money")
class MoneyTest {

  @Test
  @DisplayName("Debe crear un Money válido con valor y moneda")
  void debeCrearMoneyValido() {
    // Given
    BigDecimal valor = new BigDecimal("100.50");
    String moneda = "EUR";

    // When
    Money money = new Money(valor, moneda);

    // Then
    assertEquals(valor, money.valor());
    assertEquals(moneda, money.moneda());
  }

  @Test
  @DisplayName("Debe lanzar excepción si el valor es nulo")
  void debeRechazarValorNulo() {
    // Given
    String moneda = "EUR";

    // When / Then
    assertThrows(NullPointerException.class,
        () -> new Money(null, moneda));
  }

  @Test
  @DisplayName("Debe lanzar excepción si la moneda es nula")
  void debeRechazarMonedaNula() {
    // Given
    BigDecimal valor = BigDecimal.TEN;

    // When / Then
    assertThrows(NullPointerException.class,
        () -> new Money(valor, null));
  }

  @Test
  @DisplayName("Debe crear un Money en cero para una moneda")
  void debeCrearMoneyZero() {
    // When
    Money zero = Money.zero("USD");

    // Then
    assertEquals(BigDecimal.ZERO, zero.valor());
    assertEquals("USD", zero.moneda());
  }

  @Test
  @DisplayName("Debe sumar dos cantidades con la misma moneda")
  void debeSumarMismosTiposDeMoneda() {
    // Given
    Money m1 = new Money(new BigDecimal("10"), "EUR");
    Money m2 = new Money(new BigDecimal("5"), "EUR");

    // When
    Money resultado = m1.sumar(m2);

    // Then
    assertEquals(new BigDecimal("15"), resultado.valor());
    assertEquals("EUR", resultado.moneda());
  }

  @Test
  @DisplayName("Debe fallar al sumar cantidades con distinta moneda")
  void debeFallarAlSumarMonedasDistintas() {
    // Given
    Money eur = new Money(new BigDecimal("10"), "EUR");
    Money usd = new Money(new BigDecimal("5"), "USD");

    // When / Then
    assertThrows(IllegalArgumentException.class,
        () -> eur.sumar(usd));
  }

  @Test
  @DisplayName("Debe restar dos cantidades con la misma moneda")
  void debeRestarMismosTiposDeMoneda() {
    // Given
    Money m1 = new Money(new BigDecimal("20"), "EUR");
    Money m2 = new Money(new BigDecimal("7"), "EUR");

    // When
    Money resultado = m1.restar(m2);

    // Then
    assertEquals(new BigDecimal("13"), resultado.valor());
  }

  @Test
  @DisplayName("Debe multiplicar una cantidad por un factor")
  void debeMultiplicarCantidad() {
    // Given
    Money money = new Money(new BigDecimal("10"), "EUR");
    BigDecimal factor = new BigDecimal("2.5");

    // When
    Money resultado = money.multiplicar(factor);

    // Then
    assertEquals(new BigDecimal("25.0"), resultado.valor());
    assertEquals("EUR", resultado.moneda());
  }

  @ParameterizedTest(name = "Debe indicar si {0} es mayor que cero")
  @ValueSource(strings = {"1", "0", "-5"})
  @DisplayName("Debe evaluar correctamente si el valor es mayor que cero")
  void debeEvaluarSiEsMayorQueCero(String valor) {
    // Given
    Money money = new Money(new BigDecimal(valor), "EUR");

    // When
    boolean esMayorQueCero = money.esMayorQueCero();

    // Then
    assertEquals(new BigDecimal(valor).compareTo(BigDecimal.ZERO) > 0,
        esMayorQueCero);
  }
}
