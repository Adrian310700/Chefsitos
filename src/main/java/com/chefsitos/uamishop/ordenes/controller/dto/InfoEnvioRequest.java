package com.chefsitos.uamishop.ordenes.controller.dto;

import jakarta.validation.constraints.NotBlank;

public record InfoEnvioRequest(

  @NotBlank(message = "El número de guía es obligatorio")
  String numeroGuia,

  @NotBlank(message = "El proveedor logístico es obligatorio")
  String proveedorLogistico

) {}
