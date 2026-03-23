package com.chefsitos.uamishop.ordenes.controller.dto;

import jakarta.validation.constraints.NotBlank;

public record PagarOrdenRequest(
        @NotBlank(message = "La referencia de pago es obligatoria") String referenciaPago) {
}
