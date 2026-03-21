package com.chefsitos.uamishop.catalogo.controller.dto;

import java.time.Instant;
import java.util.UUID;

import com.chefsitos.uamishop.catalogo.domain.ProductoEstadisticas;

public record ProductoEstadisticasResponse(
    UUID productoId,
    long ventasTotales,
    long cantidadVendida,
    long vecesAgregadoAlCarrito,
    Instant ultimaVentaAt) {

  public static ProductoEstadisticasResponse from(ProductoEstadisticas estadisticas) {
    return new ProductoEstadisticasResponse(
        estadisticas.getProductoId(),
        estadisticas.getVentasTotales(),
        estadisticas.getCantidadVendida(),
        estadisticas.getVecesAgregadoAlCarrito(),
        estadisticas.getUltimaVentaAt());
  }
}
