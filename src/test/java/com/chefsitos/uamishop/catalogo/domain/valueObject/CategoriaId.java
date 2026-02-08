package com.chefsitos.uamishop.catalogo.domain.valueObject;

import java.util.UUID;

public record CategoriaId(UUID valor) {

    public static CategoriaId generar() {
        return new CategoriaId(UUID.randomUUID());
    }

    public static CategoriaId of(String id) {
        return new CategoriaId(UUID.fromString(id));
    }
}