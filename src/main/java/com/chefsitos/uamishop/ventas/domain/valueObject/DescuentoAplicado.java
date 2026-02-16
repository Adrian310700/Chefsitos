package com.chefsitos.uamishop.ventas.domain.valueObject;

import com.chefsitos.uamishop.shared.domain.valueObject.Money;
import com.chefsitos.uamishop.ventas.domain.TipoDescuento;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.math.BigDecimal;

// Este record representa un descuento aplicado a un carrito de compras. 
// Contiene el código del descuento. Ej "CUPON10"
// el tipo (CUPON, PROMOCION)
// y el valor del descuento en porcentaje (ej 10 para un 10% de descuento).

@Embeddable
public record DescuentoAplicado(String codigo, @Enumerated(EnumType.STRING) TipoDescuento tipo, BigDecimal valor) {
	private static final BigDecimal MAX_DESCUENTO = BigDecimal.valueOf(30);

	// Constructor compacto (caracteristico de los records) solo incluye
	// validaciones para asegurar que los campos no sean nulos o vacíos
	// Además, se valida que el valor del descuento sea positivo y no exceda el 30%
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
