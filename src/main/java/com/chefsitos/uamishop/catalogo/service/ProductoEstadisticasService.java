package com.chefsitos.uamishop.catalogo.service;

import java.util.UUID;

import com.chefsitos.uamishop.catalogo.domain.ProductoEstadisticas;

public class ProductoEstadisticasService {

  // crear o actualizar ProductoEstadisticas, incrementar contadores
  public void registrarVenta(UUID productoId, int cantidad) {
    throw new UnsupportedOperationException("Método no implementado");
  }

  // incrementar vecesAgregadoAlCarrito
  public void registrarAgregadoAlCarrito(UUID productoId) {
    throw new UnsupportedOperationException("Método no implementado");
  }

  // consultar por cantidadVendida descendente
  public ProductoEstadisticas obtenerMasVendidos(int limit) {
    throw new UnsupportedOperationException("Método no implementado");
  }

  public ProductoEstadisticas obtenerEstadisticas(UUID productoId) {
    throw new UnsupportedOperationException("Método no implementado");
  }
}
