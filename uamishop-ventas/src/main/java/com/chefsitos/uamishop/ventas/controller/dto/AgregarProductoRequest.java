package com.chefsitos.uamishop.ventas.controller.dto;

import java.util.UUID;

import jakarta.validation.constraints.*;

public record AgregarProductoRequest(
        @NotNull(message = "El ID del producto es obligatorio") UUID productoId,

        @NotNull(message = "La cantidad del producto es obligatoria") @Max(value = 10, message = "La cantidad del producto no puede exceder 10") @Min(value = 1, message = "La cantidad debe ser al menos 1") int cantidad) {
}
