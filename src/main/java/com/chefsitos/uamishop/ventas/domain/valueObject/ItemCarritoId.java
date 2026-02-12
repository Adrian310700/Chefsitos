package com.chefsitos.uamishop.ventas.domain.valueObject;

import java.util.UUID;

import jakarta.persistence.Embeddable;

@Embeddable
public record ItemCarritoId(UUID valor) {

    // Constructor compacto (caracteristico de los records) solo incluye
    // validaciones para asegurar que los campos no sean nulos o vacíos
    public ItemCarritoId {
        if (valor == null) {
            throw new IllegalArgumentException("ItemCarritoId no puede ser nulo");
        }
    }

    // Método para generar un nuevo ID para ItemCarrito
    public static ItemCarritoId generar() {
        return new ItemCarritoId(UUID.randomUUID());
    }

    // Método para obtener el valor del ID
    public UUID getValue() {
        return valor;
    }

}
