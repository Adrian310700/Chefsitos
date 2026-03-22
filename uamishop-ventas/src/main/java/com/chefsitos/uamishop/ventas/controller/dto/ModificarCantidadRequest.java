package com.chefsitos.uamishop.ventas.controller.dto;

import jakarta.validation.constraints.*;

public record ModificarCantidadRequest(
  @PositiveOrZero(message = "La nueva cantidad debe ser positiva o cero") 
  @Max(value = 10, message = "El producto no puede exceder las 10 unidades por carrito") 
  int nuevaCantidad) {
}
