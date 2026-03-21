package com.chefsitos.uamishop.catalogo.api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.chefsitos.uamishop.catalogo.domain.aggregate.Producto;

public record ProductoDTO(
    UUID idProducto,
    String nombreProducto,  
    String descripcion,
    BigDecimal precio,
    String moneda,
    boolean disponible,
    LocalDateTime fechaCreacion,
    UUID idCategoria) {

  public static ProductoDTO from(Producto producto) {
    return new ProductoDTO(
        producto.getProductoId().valor(),
        producto.getNombre(),
        producto.getDescripcion(),
        producto.getPrecio().cantidad(),
        producto.getPrecio().moneda(),
        producto.isDisponible(),
        producto.getFechaCreacion(),
        producto.getCategoriaId().valor());
  }
}
