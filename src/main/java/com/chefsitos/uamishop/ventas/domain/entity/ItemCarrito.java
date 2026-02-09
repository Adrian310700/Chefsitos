package com.chefsitos.uamishop.ventas.domain.entity;

import com.chefsitos.uamishop.ventas.domain.valueObject.ItemCarritoId;
import com.chefsitos.uamishop.ventas.domain.valueObject.ProductoRef;

import java.math.BigDecimal;

import com.chefsitos.uamishop.shared.Money;

public class ItemCarrito {
    private final ItemCarritoId id;
    private final ProductoRef producto;
    private Integer cantidad;
    private Money precioUnitario;

    public ItemCarrito(ItemCarritoId id, ProductoRef producto, int cantidad, Money precioUnitario) {
        // Valida que la cantidad de un producto en el carrito sea positiva
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
        }
        // Valida que la cantidad de un producto en el carrito no exceda las 10 unidades
        if (cantidad > 10) {
            throw new IllegalArgumentException("La cantidad máxima por producto son 10 unidades");
        }
        this.id = id;
        this.producto = producto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
    }

    public void actualizarCantidad(Integer nuevaCantidad) {
        // Valida que la nueva cantidad de un producto en el carrito sea positiva y no
        // exceda las 10 unidades
        if (nuevaCantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
        }
        if (nuevaCantidad > 10) {
            throw new IllegalArgumentException("La cantidad máxima por producto son 10 unidades");
        }
        this.cantidad = nuevaCantidad;
    }

    public void incrementarCantidad(Integer cantidad) {
        int nuevaCantidad = this.cantidad + cantidad;

        // Valida que la nueva cantidad de un producto en el carrito no exceda las 10
        // unidades
        if (nuevaCantidad > 10) {
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
