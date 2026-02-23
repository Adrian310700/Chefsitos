package com.chefsitos.uamishop.ordenes.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record DireccionEnvioRequest(

    @NotBlank(message = "El nombre del destinatario es obligatorio") String nombreDestinatario,

    @NotBlank(message = "La calle es obligatoria") String calle,

    @NotBlank(message = "La ciudad es obligatoria") String ciudad,

    @NotBlank(message = "El estado es obligatorio") String estado,

    @NotBlank(message = "El código postal es obligatorio") @Pattern(regexp = "\\d{5}", message = "El código postal debe tener exactamente 5 dígitos") String codigoPostal,

    @NotBlank(message = "El teléfono es obligatorio") @Pattern(regexp = "\\d{10}", message = "El teléfono debe tener exactamente 10 dígitos") String telefono,

    String instrucciones

) {
}
