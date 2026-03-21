package com.chefsitos.uamishop.catalogo.controller.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import com.chefsitos.uamishop.shared.validation.ValidUUID;

public record ProductoPatchRequest(

    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres") String nombreProducto,

    @Size(max = 500, message = "La descripción no debe exceder los 500 caracteres") String descripcion,

    @Positive(message = "El precio debe ser positivo") BigDecimal precio,

    @Size(min = 3, max = 3, message = "La moneda debe tener exactamente 3 caracteres") String moneda,

    @ValidUUID(message = "El ID de la categoría debe ser un UUID válido") String idCategoria) {
}
