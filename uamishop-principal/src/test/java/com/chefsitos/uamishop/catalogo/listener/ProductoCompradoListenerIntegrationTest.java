package com.chefsitos.uamishop.catalogo.listener;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;

import com.chefsitos.uamishop.catalogo.domain.ProductoEstadisticas;
import com.chefsitos.uamishop.catalogo.repository.ProductoEstadisticasJpaRepository;
import com.chefsitos.uamishop.shared.event.ProductoCompradoEvent;

@SpringBootTest
@DisplayName("Listener: ProductoComprado (async + Awaitility)")
public class ProductoCompradoListenerIntegrationTest {
  @Autowired
  private ApplicationEventPublisher eventPublisher;

  @Autowired
  private ProductoEstadisticasJpaRepository estadisticasRepository;

  @AfterEach
  void cleanUp() {
    estadisticasRepository.deleteAll();
  }

  @Test
  @DisplayName("Persiste item comprado, genera evento y crea nueva estadistica")
  void onProductoComprado_creaEstadistica() {
    // Cualquier producto
    UUID productoId = UUID.randomUUID();
    // Nuevo item comprado
    ProductoCompradoEvent.ItemComprado item = new ProductoCompradoEvent.ItemComprado(
        productoId,
        "SKU-TEST",
        2,
        new BigDecimal("100"),
        "MXN");
    // Genera evento
    ProductoCompradoEvent event = new ProductoCompradoEvent(
        UUID.randomUUID(),
        Instant.now(),
        UUID.randomUUID(),
        UUID.randomUUID(),
        List.of(item));

    // publica evento
    eventPublisher.publishEvent(event);

    // espera a que el listener async procese el evento
    await()
        .atMost(5, TimeUnit.SECONDS)
        .untilAsserted(() -> {

          ProductoEstadisticas estadisticas = estadisticasRepository.findById(productoId).orElse(null);

          assertNotNull(estadisticas);
          assertEquals(1, estadisticas.getVentasTotales());
          assertEquals(2, estadisticas.getCantidadVendida());
          assertNotNull(estadisticas.getUltimaVentaAt());
        });

  }

  @Test
  @DisplayName("Persiste estadisticas previamente y actualiza con el evento")
  void onProductoComprado_actualizaEstadisticasExistentes() {

    UUID productoId = UUID.randomUUID();

    // Insertar estadísticas iniciales en BD
    ProductoEstadisticas estadisticasIniciales = new ProductoEstadisticas(
        productoId,
        3,
        10,
        0,
        Instant.now().minusSeconds(3600),
        null);

    estadisticasRepository.saveAndFlush(estadisticasIniciales);

    ProductoCompradoEvent.ItemComprado item = new ProductoCompradoEvent.ItemComprado(
        productoId,
        "SKU-TEST",
        2,
        new BigDecimal("100"),
        "MXN");

    ProductoCompradoEvent event = new ProductoCompradoEvent(
        UUID.randomUUID(),
        Instant.now(),
        UUID.randomUUID(),
        UUID.randomUUID(),
        List.of(item));

    eventPublisher.publishEvent(event);

    await()
        .atMost(5, TimeUnit.SECONDS)
        .untilAsserted(() -> {

          ProductoEstadisticas estadisticas = estadisticasRepository.findById(productoId).orElse(null);

          assertNotNull(estadisticas);

          // ventasTotales se incrementa en 1
          assertEquals(4, estadisticas.getVentasTotales());

          // cantidadVendida aumenta según el evento
          assertEquals(12, estadisticas.getCantidadVendida());

          // fecha de última venta actualizada
          assertNotNull(estadisticas.getUltimaVentaAt());
        });
  }

  @Test
  @DisplayName("Genera evento a partir de multiples items")
  void onProductoComprado_multiplesItems() {

    UUID productoId1 = UUID.randomUUID();
    UUID productoId2 = UUID.randomUUID();
    UUID productoId3 = UUID.randomUUID();
    UUID productoId4 = UUID.randomUUID();

    ProductoCompradoEvent.ItemComprado item1 = new ProductoCompradoEvent.ItemComprado(
        productoId1,
        "SKU-1",
        2,
        new BigDecimal("100"),
        "MXN");

    ProductoCompradoEvent.ItemComprado item2 = new ProductoCompradoEvent.ItemComprado(
        productoId2,
        "SKU-2",
        3,
        new BigDecimal("50"),
        "MXN");

    ProductoCompradoEvent.ItemComprado item3 = new ProductoCompradoEvent.ItemComprado(
        productoId3,
        "SKU-3",
        1,
        new BigDecimal("200"),
        "MXN");
    ProductoCompradoEvent.ItemComprado item4 = new ProductoCompradoEvent.ItemComprado(
        productoId4,
        "SKU-4",
        4,
        new BigDecimal("80"),
        "MXN");

    ProductoCompradoEvent event = new ProductoCompradoEvent(
        UUID.randomUUID(),
        Instant.now(),
        UUID.randomUUID(),
        UUID.randomUUID(),
        List.of(item1, item2, item3, item4));

    // publicar evento
    eventPublisher.publishEvent(event);

    // esperar a que el listener async procese ambos productos
    await()
        .atMost(5, TimeUnit.SECONDS)
        .untilAsserted(() -> {

          ProductoEstadisticas estadisticas1 = estadisticasRepository.findById(productoId1).orElse(null);
          ProductoEstadisticas estadisticas2 = estadisticasRepository.findById(productoId2).orElse(null);
          ProductoEstadisticas estadisticas3 = estadisticasRepository.findById(productoId3).orElse(null);
          ProductoEstadisticas estadisticas4 = estadisticasRepository.findById(productoId4).orElse(null);

          assertNotNull(estadisticas1);
          assertNotNull(estadisticas2);
          assertNotNull(estadisticas3);
          assertNotNull(estadisticas4);

          assertEquals(1, estadisticas1.getVentasTotales());
          assertEquals(2, estadisticas1.getCantidadVendida());
          assertEquals(1, estadisticas2.getVentasTotales());
          assertEquals(3, estadisticas2.getCantidadVendida());
          assertEquals(1, estadisticas3.getVentasTotales());
          assertEquals(1, estadisticas3.getCantidadVendida());
          assertEquals(1, estadisticas4.getVentasTotales());
          assertEquals(4, estadisticas4.getCantidadVendida());
        });
  }

  @Test
  @DisplayName("Un mismo item con muchas unidades compradas")
  void onProductoComprado_multiplesUnidadesDeUnProducto() {

    UUID productoId = UUID.randomUUID();

    // Crear evento con muchas unidades
    ProductoCompradoEvent.ItemComprado item = new ProductoCompradoEvent.ItemComprado(
        productoId,
        "SKU-TEST",
        200,
        new BigDecimal("100"),
        "MXN");

    ProductoCompradoEvent event = new ProductoCompradoEvent(
        UUID.randomUUID(),
        Instant.now(),
        UUID.randomUUID(),
        UUID.randomUUID(),
        List.of(item));

    eventPublisher.publishEvent(event);

    await()
        .atMost(5, TimeUnit.SECONDS)
        .untilAsserted(() -> {

          ProductoEstadisticas estadisticas = estadisticasRepository.findById(productoId).orElse(null);

          assertNotNull(estadisticas);

          assertEquals(1, estadisticas.getVentasTotales());
          assertEquals(200, estadisticas.getCantidadVendida());
          assertNotNull(estadisticas.getUltimaVentaAt());
        });
  }

  @Test
  @DisplayName("Genera evento sin items y no genera ninguna estadistica")
  void onProductoComprado_listaItemsVacia() {

    ProductoCompradoEvent event = new ProductoCompradoEvent(
        UUID.randomUUID(),
        Instant.now(),
        UUID.randomUUID(),
        UUID.randomUUID(),
        List.of());

    eventPublisher.publishEvent(event);
    await()
        .atMost(3, TimeUnit.SECONDS)
        .untilAsserted(() -> {
          // Comprueba que no hay estadisticas persistidas
          long total = estadisticasRepository.count();
          assertEquals(0, total);
        });
  }
}
