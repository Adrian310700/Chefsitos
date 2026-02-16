package com.chefsitos.uamishop.catalogo.controller.dto;

import jakarta.validation.constraints.NotBlank;

public record CategoriaRequest(

    @NotBlank String nombreCategoria,
    String descripcion)

{
}
