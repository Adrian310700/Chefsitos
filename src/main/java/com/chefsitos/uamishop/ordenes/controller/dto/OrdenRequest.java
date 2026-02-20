package com.chefsitos.uamishop.ordenes.controller.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.math.BigDecimal;
import java.util.List;

public record OrdenRequest(

  @NotBlank String clienteId,

  @Valid DireccionEnvioRequest direccion,

  @NotEmpty @Valid List<ItemRequest> items

) {

  public record ItemRequest(
    @NotBlank String productoId,
    @NotBlank String nombreProducto,
    @NotBlank String sku,
    int cantidad,
    BigDecimal precioUnitario
  ) {
  }
}
