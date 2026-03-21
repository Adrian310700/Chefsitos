package com.chefsitos.uamishop.catalogo.listener;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
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
import com.chefsitos.uamishop.shared.event.ProductoAgregadoAlCarritoEvent;

import java.time.Instant;

/**
 * Test de integración que demuestra el uso de Awaitility
 * para verificar efectos secundarios de eventos asíncronos.
 *
 * El patrón es:
 * 1. Publicar un evento con ApplicationEventPublisher
 * 2. Usar await().atMost(...) para esperar a que el listener @Async lo procese
 * 3. Verificar el efecto secundario (en este caso, la estadística guardada en
 * BD)
 */
@SpringBootTest
@DisplayName("Listener: ProductoAgregadoAlCarrito (async + Awaitility)")
class ProductoAgregadoAlCarritoListenerIntegrationTest {

  @Autowired
  private ApplicationEventPublisher eventPublisher;

  @Autowired
  private ProductoEstadisticasJpaRepository estadisticasRepository;

  @AfterEach
  void cleanUp() {
    estadisticasRepository.deleteAll();
  }

  @Test
  @DisplayName("al publicar ProductoAgregadoAlCarritoEvent, se registra la estadística en BD")
  void alPublicarEvento_seRegistraEstadistica() {
    // Arrange: preparar el evento
    UUID productoId = UUID.randomUUID();
    UUID carritoId = UUID.randomUUID();

    ProductoAgregadoAlCarritoEvent evento = new ProductoAgregadoAlCarritoEvent(
        UUID.randomUUID(),
        Instant.now(),
        productoId,
        carritoId,
        3,
        new BigDecimal("150.00"),
        "MXN");

    // Act: publicar el evento (el listener es @Async, se ejecuta en otro hilo)
    eventPublisher.publishEvent(evento);

    // Assert: usar Awaitility para esperar a que el listener procese el evento
    await()
        .atMost(5, TimeUnit.SECONDS) // timeout máximo
        .pollInterval(200, TimeUnit.MILLISECONDS) // cada cuánto revisar
        .untilAsserted(() -> {
          // Verificar que la estadística se guardó en BD
          ProductoEstadisticas estadisticas = estadisticasRepository
              .findById(productoId)
              .orElse(null);

          assertNotNull(estadisticas, "La estadística debió haberse creado en BD");
          assertEquals(1, estadisticas.getVecesAgregadoAlCarrito(),
              "Debe tener 1 vez agregado al carrito");
          assertNotNull(estadisticas.getUltimaAgregadoAlCarritoAt(),
              "Debe tener la fecha de última agregación");
        });
  }

  @Test
  @DisplayName("al publicar eventos de distintos productos, cada uno registra su estadística")
  void alPublicarMultiplesEventos_cadaProductoRegistraEstadistica() {
    // Arrange
    UUID productoId1 = UUID.randomUUID();
    UUID productoId2 = UUID.randomUUID();
    UUID productoId3 = UUID.randomUUID();

    // Act: publicar un evento por cada producto
    for (UUID productoId : List.of(productoId1, productoId2, productoId3)) {
      eventPublisher.publishEvent(new ProductoAgregadoAlCarritoEvent(
          UUID.randomUUID(),
          Instant.now(),
          productoId,
          UUID.randomUUID(),
          1,
          new BigDecimal("100.00"),
          "MXN"));
    }

    // Assert: esperar a que los 3 listeners @Async terminen
    await()
        .atMost(5, TimeUnit.SECONDS)
        .pollInterval(200, TimeUnit.MILLISECONDS)
        .untilAsserted(() -> {
          assertEquals(3, estadisticasRepository.count(),
              "Deben existir 3 registros de estadísticas");

          for (UUID productoId : List.of(productoId1, productoId2, productoId3)) {
            ProductoEstadisticas estadisticas = estadisticasRepository
                .findById(productoId).orElse(null);
            assertNotNull(estadisticas,
                "Debe existir estadística para productoId=" + productoId);
            assertEquals(1, estadisticas.getVecesAgregadoAlCarrito());
          }
        });
  }

  @Test
  @DisplayName("Persiste previamente estadisticas y actualiza con el evento")
  void alPublicarEvento_conEstadisticasExistentes_seActualizaContador() {
    UUID productoId = UUID.randomUUID();
    // Insertar estadísticas iniciales
    ProductoEstadisticas estadisticasIniciales = new ProductoEstadisticas(
        productoId,
        0,
        0,
        5,
        null,
        Instant.now().minusSeconds(3600));

    estadisticasRepository.saveAndFlush(estadisticasIniciales);

    ProductoAgregadoAlCarritoEvent event = new ProductoAgregadoAlCarritoEvent(
        UUID.randomUUID(),
        Instant.now(),
        productoId,
        UUID.randomUUID(),
        1,
        new BigDecimal("100.00"),
        "MXN");

    eventPublisher.publishEvent(event);

    await()
        .atMost(5, TimeUnit.SECONDS)
        .untilAsserted(() -> {

          ProductoEstadisticas estadisticas = estadisticasRepository.findById(productoId).orElse(null);

          assertNotNull(estadisticas);

          assertEquals(6, estadisticas.getVecesAgregadoAlCarrito());
          assertNotNull(estadisticas.getUltimaAgregadoAlCarritoAt());
        });
  }
}
