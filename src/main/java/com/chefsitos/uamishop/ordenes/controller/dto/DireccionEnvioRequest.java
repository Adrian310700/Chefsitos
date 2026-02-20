package com.chefsitos.uamishop.ordenes.controller.dto;

import jakarta.validation.constraints.NotBlank;

public record DireccionEnvioRequest(

  @NotBlank String nombreDestinatario,
  @NotBlank String calle,
  @NotBlank String ciudad,
  @NotBlank String estado,
  @NotBlank String codigoPostal,
  @NotBlank String pais,
  @NotBlank String telefono,
  String instrucciones
) {
}
