package com.chefsitos.uamishop.catalogo.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CategoriaRequest(

        @NotBlank(message = "El nombre es obligatorio") @Size(max = 200, message = "El nombre no puede exceder los 200 caracteres") String nombreCategoria,

        @Size(max = 1000, message = "La descripción no puede exceder los 1000 caracteres") String descripcion,

        UUID categoriaPadreId) {
}
