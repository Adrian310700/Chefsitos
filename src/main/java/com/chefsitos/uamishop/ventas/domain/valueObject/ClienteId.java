package com.chefsitos.uamishop.ventas.domain.valueObject;

import java.util.UUID;

public record ClienteId(UUID valor) {
    // Validación para asegurar que el ID del cliente no sea nulo
    public ClienteId {
        if (valor == null) {
            throw new IllegalArgumentException("El ID del cliente no puede ser nulo");
        }
    }

    public static ClienteId of(String id) {
        return new ClienteId(UUID.fromString(id));
    }

    // Obtiene el valor del ID del cliente
    public UUID getValue() {
        return valor;
    }
}
