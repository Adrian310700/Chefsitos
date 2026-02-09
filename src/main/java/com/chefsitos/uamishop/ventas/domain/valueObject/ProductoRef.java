package com.chefsitos.uamishop.ventas.domain.valueObject;

import java.util.UUID;

public record ProductoRef(UUID productoId, String nombreProducto, String sku) {
    // Validaciones para asegurar que los campos no sean nulos o vacíos
    public ProductoRef {
        if (productoId == null) {
            throw new IllegalArgumentException("productoId no puede ser nulo");
        }
        if (nombreProducto == null || nombreProducto.isBlank()) {
            throw new IllegalArgumentException("nombreProducto inválido");
        }
        if (sku == null || sku.isBlank()) {
            throw new IllegalArgumentException("sku inválido");
        }
    }

    public UUID getProductoId() {
        return productoId;
    }
}
