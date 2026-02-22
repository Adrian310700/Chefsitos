package com.chefsitos.uamishop.ordenes.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DireccionEnvioRequest(

  @NotBlank(message = "El nombre del destinatario es obligatorio")
  String nombreDestinatario,

  @NotBlank(message = "La calle es obligatoria")
  String calle,

  @NotBlank(message = "La ciudad es obligatoria")
  String ciudad,

  @NotBlank(message = "El estado es obligatorio")
  String estado,

  @NotBlank(message = "El código postal es obligatorio")
  @Size(min = 5, max = 10, message = "El CP debe tener entre 5 y 10 caracteres")
  String codigoPostal,

  @NotBlank(message = "El país es obligatorio")
  String pais,

  @NotBlank(message = "El teléfono es obligatorio")
  String telefono,

  String instrucciones

) {}
