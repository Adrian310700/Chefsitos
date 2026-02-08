package com.chefsitos.uamishop.catalogo.domain.valueObject;

import java.util.UUID;

public record ProductoId(UUID valor) {

    public static ProductoId generar() {
        return new ProductoId(UUID.randomUUID());
    }

    public static ProductoId of(String id) {
        return new ProductoId(UUID.fromString(id));
    }
}