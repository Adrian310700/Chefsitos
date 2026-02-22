package com.chefsitos.uamishop.ordenes.controller.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record OrdenRequest(

    @NotNull(message = "El ID del cliente es obligatorio") UUID clienteId,

    @NotNull(message = "La dirección de envío es obligatoria") @Valid DireccionEnvioRequest direccion,

    @NotEmpty(message = "La orden debe tener al menos un ítem") @Valid List<ItemOrdenRequest> items

) {

  public record ItemOrdenRequest(

      @NotBlank(message = "El ID del producto es obligatorio") @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$", message = "El ID del producto debe ser un UUID válido") String productoId,

      @Positive(message = "La cantidad debe ser mayor a 0") int cantidad

  ) {
  }
}
