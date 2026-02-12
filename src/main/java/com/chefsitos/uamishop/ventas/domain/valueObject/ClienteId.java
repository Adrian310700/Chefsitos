package com.chefsitos.uamishop.ventas.domain.valueObject;

import java.util.UUID;

import jakarta.persistence.Embeddable;

@Embeddable
public record ClienteId(UUID valor) {

    // Constructor compacto (caracteristico de los records) solo incluye
    // validaciones para asegurar que los campos no sean nulos o vacíos
    public ClienteId {
        if (valor == null) {
            throw new IllegalArgumentException("El ID del cliente no puede ser nulo");
        }
    }

    // Método para crear un nuevo ClienteId con un UUID generado
    public static ClienteId of(String id) {
        return new ClienteId(UUID.fromString(id));
    }

    // Obtiene el valor del ID del cliente
    public UUID getValue() {
        return valor;
    }
}
