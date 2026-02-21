package com.chefsitos.uamishop.ventas.controller.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record CarritoRequest(@NotNull UUID clienteId) {
}
