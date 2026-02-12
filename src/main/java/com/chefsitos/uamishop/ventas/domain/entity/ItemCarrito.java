package com.chefsitos.uamishop.ventas.domain.entity;

import com.chefsitos.uamishop.ventas.domain.valueObject.ItemCarritoId;
import com.chefsitos.uamishop.ventas.domain.valueObject.ProductoRef;
import com.chefsitos.uamishop.shared.domain.valueObject.Money;

import java.math.BigDecimal;

import jakarta.persistence.Entity;
import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;

@Entity
public class ItemCarrito {
	// Se define una cantidad máxima de unidades por producto en el
	// carrito para cumplir con la RN-VEN-02
	private static final int MAX_UNIDADES = 10;

	@EmbeddedId
	private ItemCarritoId id;
	@Embedded
	private ProductoRef producto;
	private Integer cantidad;
	@Embedded
	private Money precioUnitario;

	// Constructor vacío para JPA
	protected ItemCarrito() {
	}

	// Constructor privado para forzar el uso del método de creación con
	// validaciones de negocio
	public ItemCarrito(ItemCarritoId id, ProductoRef producto, Integer cantidad, Money precioUnitario) {
		// Valida que la cantidad de un producto en el carrito sea positiva
		// RN-VEN-01
		if (cantidad <= 0) {
			throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
		}
		// Valida que la cantidad de un producto en el carrito no exceda las 10 unidades
		// RN-VEN-02
		if (cantidad > MAX_UNIDADES) {
			throw new IllegalArgumentException("La cantidad máxima por producto son 10 unidades");
		}
		this.id = id;
		this.producto = producto;
		this.cantidad = cantidad;
		this.precioUnitario = precioUnitario;
	}

	public void actualizarCantidad(Integer nuevaCantidad) {
		// Valida que la nueva cantidad de un producto en el carrito sea positiva y no
		// exceda las 10 unidades para cumplir con las RN-VEN-01 y RN-VEN-02
		if (nuevaCantidad <= 0) {
			throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
		}
		if (nuevaCantidad > MAX_UNIDADES) {
			throw new IllegalArgumentException("La cantidad máxima por producto son 10 unidades");
		}
		this.cantidad = nuevaCantidad;
	}

	public void incrementarCantidad(Integer cantidad) {
		Integer nuevaCantidad = this.cantidad + cantidad;
		// Valida que la nueva cantidad de un producto en el carrito no exceda las 10
		// unidades para cumplir con la RN-VEN-02
		if (nuevaCantidad > MAX_UNIDADES) {
			throw new IllegalArgumentException("La cantidad máxima por producto son 10 unidades");
		}
		this.cantidad = nuevaCantidad;
	}

	public Money calcularSubtotal() {
		return precioUnitario.multiplicar(BigDecimal.valueOf(cantidad));
	}

	public ProductoRef getProducto() {
		return producto;
	}
}
