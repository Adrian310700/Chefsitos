package com.chefsitos.uamishop.catalogo.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record ProductoRequest(

    @NotBlank String nombreProducto,
    @NotBlank String descripcion,
    @NotNull @Positive BigDecimal precio,
    @NotBlank String moneda,
    @NotNull String idCategoria)

{
}
