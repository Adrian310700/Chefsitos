package com.chefsitos.uamishop.catalogo.domain.valueObject;

import java.util.UUID;

public record ImagenId(UUID valor) {

    public static ImagenId generar() {
        return new ImagenId(UUID.randomUUID());
    }

    public static ImagenId of(String id) {
        return new ImagenId(UUID.fromString(id));
    }
}