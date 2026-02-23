package com.chefsitos.uamishop.ordenes.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CancelarOrdenRequest(
        @NotBlank(message = "El motivo es obligatorio") @Size(min = 10, message = "El motivo debe tener al menos 10 caracteres") String motivo) {
}
