package com.chefsitos.uamishop.shared.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ProductoCompradoEvent(
  UUID eventId,
  Instant occurredAt,
  UUID ordenId,
  UUID clienteId,
  List<ItemComprado> items
) {

  public record ItemComprado(
    UUID productoId,
    String sku,
    int cantidad,
    BigDecimal precioUnitario,
    String moneda
  ) {}

}
