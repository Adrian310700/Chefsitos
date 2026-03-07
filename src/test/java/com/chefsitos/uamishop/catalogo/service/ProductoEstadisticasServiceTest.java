package com.chefsitos.uamishop.catalogo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.InvalidDataAccessApiUsageException;

import com.chefsitos.uamishop.catalogo.domain.ProductoEstadisticas;
import com.chefsitos.uamishop.catalogo.repository.ProductoEstadisticasJpaRepository;
import com.chefsitos.uamishop.shared.exception.ResourceNotFoundException;

import jakarta.transaction.Transactional;

@SpringBootTest
@Transactional
class ProductoEstadisticasServiceTest {

  @Autowired
  private ProductoEstadisticasService estadisticasService;

  @Autowired
  private ProductoEstadisticasJpaRepository estadisticasRepository;

  @Test
  void registrarVenta_DesdeProductoNuevo_Correctamente() {

    UUID productoId = UUID.randomUUID();
    int cantidad = 5;

    estadisticasService.registrarVenta(productoId, cantidad);

    ProductoEstadisticas estadisticas = estadisticasRepository.findById(productoId).orElseThrow();

    assertEquals(productoId, estadisticas.getProductoId());
    assertEquals(1, estadisticas.getVentasTotales());
    assertEquals(cantidad, estadisticas.getCantidadVendida());
    assertNotNull(estadisticas.getUltimaVentaAt());
  }

  @Test
  void registrarVenta_DesdeProductoExistente_Correctamente() {

    UUID productoId = UUID.randomUUID();

    ProductoEstadisticas estadisticas = new ProductoEstadisticas(productoId, 2, 10, 0, null, null);

    estadisticasRepository.save(estadisticas);

    estadisticasService.registrarVenta(productoId, 3);

    ProductoEstadisticas actualizado = estadisticasRepository.findById(productoId).orElseThrow();

    assertEquals(3, actualizado.getVentasTotales());
    assertEquals(13, actualizado.getCantidadVendida());
    assertNotNull(actualizado.getUltimaVentaAt());
  }

  @Test
  void registrarVenta_productoIdNulo_Excepcion() {

    assertThrows(InvalidDataAccessApiUsageException.class,
        () -> estadisticasService.registrarVenta(null, 5));
  }

  @Test
  void registrarAgregadoAlCarrito_DesdeProductoExistente_Correctamente() {

    UUID productoId = UUID.randomUUID();

    ProductoEstadisticas estadisticas = new ProductoEstadisticas(
        productoId,
        0,
        0,
        2,
        null,
        null);

    estadisticasRepository.save(estadisticas);

    estadisticasService.registrarAgregadoAlCarrito(productoId);

    ProductoEstadisticas resultado = estadisticasRepository.findById(productoId).orElseThrow();

    assertEquals(3, resultado.getVecesAgregadoAlCarrito());
    assertNotNull(resultado.getUltimaAgregadoAlCarritoAt());
  }

  @Test
  void registrarAgregadoAlCarrito_DesdeProductoNuevo_Correctamente() {

    UUID productoId = UUID.randomUUID();

    estadisticasService.registrarAgregadoAlCarrito(productoId);

    ProductoEstadisticas resultado = estadisticasRepository.findById(productoId).orElseThrow();

    assertEquals(productoId, resultado.getProductoId());
    assertEquals(1, resultado.getVecesAgregadoAlCarrito());
    assertNotNull(resultado.getUltimaAgregadoAlCarritoAt());
  }

  @Test
  void obtenerMasVendidos_OrdenadosPorCantidadVendida_Correctamente() {

    ProductoEstadisticas p1 = new ProductoEstadisticas(
        UUID.randomUUID(), 1, 10, 0, Instant.now(), null);

    ProductoEstadisticas p2 = new ProductoEstadisticas(
        UUID.randomUUID(), 1, 30, 0, Instant.now(), null);

    ProductoEstadisticas p3 = new ProductoEstadisticas(
        UUID.randomUUID(), 1, 20, 0, Instant.now(), null);

    estadisticasRepository.saveAll(List.of(p1, p2, p3));

    List<ProductoEstadisticas> resultado = estadisticasService.obtenerMasVendidos(3);

    assertEquals(3, resultado.size());

    assertEquals(30, resultado.get(0).getCantidadVendida());
    assertEquals(20, resultado.get(1).getCantidadVendida());
    assertEquals(10, resultado.get(2).getCantidadVendida());
  }

  @Test
  void obtenerMasVendidos_ComprobarParametroLimite_Correctamente() {

    ProductoEstadisticas p1 = new ProductoEstadisticas(
        UUID.randomUUID(), 1, 50, 0, Instant.now(), null);

    ProductoEstadisticas p2 = new ProductoEstadisticas(
        UUID.randomUUID(), 1, 40, 0, Instant.now(), null);

    ProductoEstadisticas p3 = new ProductoEstadisticas(
        UUID.randomUUID(), 1, 30, 0, Instant.now(), null);

    estadisticasRepository.saveAll(List.of(p1, p2, p3));

    List<ProductoEstadisticas> resultado = estadisticasService.obtenerMasVendidos(2);

    assertEquals(2, resultado.size());

    assertEquals(50, resultado.get(0).getCantidadVendida());
    assertEquals(40, resultado.get(1).getCantidadVendida());
  }

  @Test
  void obtenerMasVendidos_ComprobarLimiteMayor_Correctamente() {

    ProductoEstadisticas p1 = new ProductoEstadisticas(
        UUID.randomUUID(), 1, 15, 0, Instant.now(), null);

    ProductoEstadisticas p2 = new ProductoEstadisticas(
        UUID.randomUUID(), 1, 5, 0, Instant.now(), null);

    estadisticasRepository.saveAll(List.of(p1, p2));

    List<ProductoEstadisticas> resultado = estadisticasService.obtenerMasVendidos(10);

    assertEquals(2, resultado.size());
  }

  @Test
  void obtenerEstadisticas_Correctamente() {

    UUID productoId = UUID.randomUUID();

    ProductoEstadisticas estadisticas = new ProductoEstadisticas(
        productoId,
        5,
        20,
        3,
        Instant.now(),
        Instant.now());

    estadisticasRepository.save(estadisticas);

    ProductoEstadisticas resultado = estadisticasService.obtenerEstadisticas(productoId);

    assertNotNull(resultado);
    assertEquals(productoId, resultado.getProductoId());
    assertEquals(5, resultado.getVentasTotales());
    assertEquals(20, resultado.getCantidadVendida());
    assertEquals(3, resultado.getVecesAgregadoAlCarrito());
  }

  @Test
  void obtenerEstadisticas_ProductoInexistente_Excepcion() {

    UUID productoId = UUID.randomUUID();

    ResourceNotFoundException exception = assertThrows(
        ResourceNotFoundException.class,
        () -> estadisticasService.obtenerEstadisticas(productoId));

    assertTrue(exception.getMessage().contains(productoId.toString()));
  }
}
