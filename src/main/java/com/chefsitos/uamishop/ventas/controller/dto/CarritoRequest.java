package com.chefsitos.uamishop.ventas.controller.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record CarritoRequest(
  @NotNull (message = "El ID del cliente es obligatorio") 
  UUID clienteId) {
}
