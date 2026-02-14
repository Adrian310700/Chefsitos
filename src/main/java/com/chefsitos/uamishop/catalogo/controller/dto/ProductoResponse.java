package com.chefsitos.uamishop.catalogo.controller.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.chefsitos.uamishop.catalogo.domain.aggregate.Producto;
import com.chefsitos.uamishop.catalogo.domain.entity.Categoria;

public record ProductoResponse(
    UUID idProducto,
    String nombreProducto,
    String descripcion,
    BigDecimal precio,
    String moneda,
    boolean disponible,
    LocalDateTime fechaCreacion,
    UUID idCategoria,
    String nombreCategoria) {

  public static ProductoResponse from(Producto producto) {
    return new ProductoResponse(
        producto.getProductoId().valor(),
        producto.getNombre(),
        producto.getDescripcion(),
        producto.getPrecio().valor(),
        producto.getPrecio().moneda(),
        producto.isDisponible(),
        producto.getFechaCreacion(),
        producto.getCategoriaId().valor(),
        );
  }
}
