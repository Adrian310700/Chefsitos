package com.chefsitos.uamishop.ordenes.controller.dto;

import jakarta.validation.constraints.NotBlank;

public record InfoEnvioRequest(

  @NotBlank String numeroGuia
) {
}
