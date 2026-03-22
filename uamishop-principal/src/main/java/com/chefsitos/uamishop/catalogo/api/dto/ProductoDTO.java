package com.chefsitos.uamishop.catalogo.api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ProductoDTO(
    UUID idProducto,
    String nombreProducto,
    String descripcion,
    BigDecimal precio,
    String moneda,
    boolean disponible,
    LocalDateTime fechaCreacion,
    UUID idCategoria) {
}
