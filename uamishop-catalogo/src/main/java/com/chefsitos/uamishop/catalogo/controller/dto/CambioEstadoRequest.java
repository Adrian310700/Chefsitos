package com.chefsitos.uamishop.catalogo.controller.dto;

import jakarta.validation.constraints.NotNull;

public record CambioEstadoRequest(
    @NotNull(message = "El estado es obligatorio") Boolean activo) {
}
