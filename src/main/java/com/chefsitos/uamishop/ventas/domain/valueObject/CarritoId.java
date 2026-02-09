package com.chefsitos.uamishop.ventas.domain.valueObject;

import java.util.UUID;

public record CarritoId(UUID valor) {
    // Validación para asegurar que el ID del carrito no sea nulo
    public CarritoId {
        if (valor == null) {
            throw new IllegalArgumentException("El ID del carrito no puede ser nulo");
        }
    }

    public static CarritoId generar() {
        return new CarritoId(UUID.randomUUID());
    }

    public UUID getValue() {
        return valor;
    }
}
