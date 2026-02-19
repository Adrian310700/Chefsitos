package com.chefsitos.uamishop.ventas.domain.valueObject;

import com.chefsitos.uamishop.shared.domain.valueObject.Money;
import com.chefsitos.uamishop.ventas.domain.TipoDescuento;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.math.BigDecimal;

// Este record representa un descuento aplicado a un carrito de compras.
// Contiene el código del descuento (ej "CUPON10"),
// el tipo (CUPON, PROMOCION),
// el valor del descuento en porcentaje (ej 10 para un 10% de descuento),
// y el monto descontado calculado al aplicar el descuento.

@Embeddable
public record DescuentoAplicado(
    String codigo,
    @Enumerated(EnumType.STRING) TipoDescuento tipo,
    BigDecimal valor,
    Money montoDescontado) {

  private static final BigDecimal MAX_DESCUENTO = BigDecimal.valueOf(30);

  // Constructor compacto con validaciones
  // RN-VEN-16: El descuento no puede ser mayor al 30% del subtotal
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
    if (valor.compareTo(MAX_DESCUENTO) > 0) {
      throw new IllegalArgumentException("El descuento no puede exceder el 30% de la compra");
    }
  }

  // Constructor simplificado sin montoDescontado (se calcula después)
  public static DescuentoAplicado crear(String codigo, TipoDescuento tipo, BigDecimal valor) {
    return new DescuentoAplicado(codigo, tipo, valor, null);
  }

  // Método para calcular el monto del descuento y devolver un nuevo
  // DescuentoAplicado con el monto
  public DescuentoAplicado conMontoCalculado(Money subtotal) {
    Money descuento = calcularDescuento(subtotal);
    return new DescuentoAplicado(this.codigo, this.tipo, this.valor, descuento);
  }

  // Método para calcular el monto del descuento a aplicar sobre un subtotal dado
  public Money calcularDescuento(Money subtotal) {
    // Si el subtotal es cero o negativo, no se aplica ningún descuento
    if (!subtotal.esMayorQueCero()) {
      return Money.zero(subtotal.moneda());
    }
    BigDecimal porcentajeDecimal = valor.divide(BigDecimal.valueOf(100));
    Money descuento = subtotal.multiplicar(porcentajeDecimal);
    return descuento;
  }

}
