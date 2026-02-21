package com.chefsitos.uamishop.ordenes.controller.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record OrdenRequest(

  @NotNull(message = "El ID del cliente es obligatorio")
  UUID clienteId,

  @NotNull(message = "La dirección de envío es obligatoria")
  @Valid
  DireccionEnvioRequest direccion,

  @NotEmpty(message = "La orden debe tener al menos un ítem")
  @Valid
  List<ItemOrdenRequest> items

) {

  public record ItemOrdenRequest(

    @NotBlank(message = "El ID del producto es obligatorio")
    String productoId,

    @NotBlank(message = "El nombre del producto es obligatorio")
    String nombreProducto,

    @NotBlank(message = "El SKU es obligatorio")
    String sku,

    @Positive(message = "La cantidad debe ser mayor a 0")
    int cantidad,

    @NotNull(message = "El precio unitario es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor a 0")
    @Digits(integer = 10, fraction = 2, message = "Formato de precio inválido")
    BigDecimal precioUnitario

  ) {}
}
