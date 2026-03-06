package com.chefsitos.uamishop.catalogo.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.chefsitos.uamishop.catalogo.domain.ProductoEstadisticas;
import com.chefsitos.uamishop.catalogo.repository.ProductoEstadisticasJpaRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductoEstadisticasService {

  private final ProductoEstadisticasJpaRepository estadisticasRepository;

  public void registrarVenta(UUID productoId, int cantidad) {
    ProductoEstadisticas estadisticas = estadisticasRepository.findById(productoId)
        .orElse(new ProductoEstadisticas(productoId, 0, 0, 0, null, null));

    estadisticas.setVentasTotales(estadisticas.getVentasTotales() + 1);
    estadisticas.setCantidadVendida(estadisticas.getCantidadVendida() + cantidad);
    estadisticas.setUltimaVentaAt(Instant.now());

    estadisticasRepository.save(estadisticas);
  }

  public void registrarAgregadoAlCarrito(UUID productoId) {
    ProductoEstadisticas estadisticas = estadisticasRepository.findById(productoId)
        .orElse(new ProductoEstadisticas(productoId, 0, 0, 0, null, null));

    estadisticas.setVecesAgregadoAlCarrito(estadisticas.getVecesAgregadoAlCarrito() + 1);
    estadisticas.setUltimaAgregadoAlCarritoAt(Instant.now());

    estadisticasRepository.save(estadisticas);
  }

  public List<ProductoEstadisticas> obtenerMasVendidos(int limit) {
    return estadisticasRepository.findMasVendidos(limit);
  }

  public Optional<ProductoEstadisticas> obtenerEstadisticas(UUID productoId) {
    return estadisticasRepository.findById(productoId);
  }
}
