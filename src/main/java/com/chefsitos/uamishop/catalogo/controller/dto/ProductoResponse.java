package com.chefsitos.uamishop.catalogo.controller.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ProductoResponse(
    UUID idProducto,
    String nombreProducto,
    String descripcion,
    BigDecimal precio,
    String moneda,
    String disponible,
    LocalDateTime fechaCreacion,
    UUID idCategoria,
    String nombreCategoria) {
}
