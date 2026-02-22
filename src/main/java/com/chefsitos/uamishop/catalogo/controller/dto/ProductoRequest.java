package com.chefsitos.uamishop.catalogo.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record ProductoRequest(

        @NotBlank(message = "El nombre del producto es obligatorio") @Size(max = 200, message = "El nombre no puede exceder los 200 caracteres") String nombreProducto,

        @NotBlank(message = "La descripción es obligatoria") @Size(max = 1000, message = "La descripción no puede exceder los 1000 caracteres") String descripcion,

        @NotNull(message = "El precio es obligatorio") @Positive(message = "El precio debe ser positivo") BigDecimal precio,

        // ISO 4217: codigo alfabetico de 3 letras para representar divisas
        @NotBlank(message = "La moneda es obligatoria") @Size(min = 3, max = 3, message = "La moneda debe tener exactamente 3 caracteres") String moneda,

        @NotBlank(message = "El ID de la categoría es obligatorio") @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$", message = "El ID de la categoría debe ser un UUID válido") String idCategoria) {
}
