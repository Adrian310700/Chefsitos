package com.chefsitos.uamishop.ordenes.controller.dto;

import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record CrearOrdenDesdeCarritoRequest(

        @NotNull(message = "El ID del carrito es obligatorio") UUID carritoId,

        @NotNull(message = "La dirección de envío es obligatoria") @Valid DireccionEnvioRequest direccion) {
}
