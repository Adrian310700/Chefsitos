package com.chefsitos.uamishop.ventas.controller.dto;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.*;

public record AgregarProductoRequest(
    @NotNull(message = "El ID del producto es obligatorio") 
    UUID productoId,

    @NotEmpty(message = "El nombre del producto es obligatorio") 
    String nombreProducto,

    @NotEmpty(message = "El SKU del producto es obligatorio") 
    String sku,

    @NotNull(message = "La cantidad del producto es obligatoria") 
    @Max(value = 10, message = "La cantidad del producto no puede exceder 10") 
    @Min(value = 0, message = "La cantidad del producto no puede ser negativa") 
    int cantidad,

    @NotNull(message = "El precio unitario del producto es obligatorio") 
    @Positive(message = "El precio unitario del producto debe ser mayor a 0") 
    BigDecimal precioUnitario,

    @NotEmpty(message = "La moneda del producto es obligatoria") String moneda) {
}
