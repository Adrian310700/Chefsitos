package com.chefsitos.uamishop.ventas.domain.valueObject;

import java.util.UUID;

public record ItemCarritoId(UUID valor) {
    // Validación para valor no nulo
    public ItemCarritoId {
        if (valor == null) {
            throw new IllegalArgumentException("ItemCarritoId no puede ser nulo");
        }
    }

    public static ItemCarritoId generar() {
        return new ItemCarritoId(UUID.randomUUID());
    }

    public UUID getValue() {
        return valor;
    }
}
