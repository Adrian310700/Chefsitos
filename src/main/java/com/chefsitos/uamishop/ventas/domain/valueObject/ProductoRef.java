package com.chefsitos.uamishop.ventas.domain.valueObject;

import com.chefsitos.uamishop.shared.domain.valueObject.ProductoId;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;

@Embeddable
public record ProductoRef(
        @Embedded @AttributeOverride(name = "valor", column = @Column(name = "producto_id")) ProductoId productoId,
        String nombreProducto,
        String sku) {

    // Constructor compacto con validaciones
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
        // RN-VO-05: El SKU debe tener formato AAA-000 (3 letras, guión, 3 números)
        if (!sku.matches("[A-Z]{3}-\\d{3}")) {
            throw new IllegalArgumentException(
                    "El SKU debe tener formato AAA-000 (3 letras mayúsculas, guión, 3 números)");
        }
    }

    public ProductoId getProductoId() {
        return productoId;
    }
}
