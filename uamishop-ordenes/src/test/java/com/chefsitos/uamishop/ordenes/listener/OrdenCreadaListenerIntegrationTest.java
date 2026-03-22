package com.chefsitos.uamishop.ventas.listener;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;

import com.chefsitos.uamishop.shared.domain.valueObject.CarritoId;
import com.chefsitos.uamishop.shared.domain.valueObject.ClienteId;
import com.chefsitos.uamishop.shared.domain.valueObject.Money;
import com.chefsitos.uamishop.shared.domain.valueObject.ProductoId;
import com.chefsitos.uamishop.shared.event.OrdenCreadaEvent;
import com.chefsitos.uamishop.ventas.domain.aggregate.Carrito;
import com.chefsitos.uamishop.ventas.domain.enumeration.EstadoCarrito;
import com.chefsitos.uamishop.ventas.domain.valueObject.ProductoRef;
import com.chefsitos.uamishop.ventas.repository.CarritoJpaRepository;

/**
 * Test de integración que verifica que el listener cambia el estado del carrito
 * de EN_CHECKOUT a COMPLETADO
 * al recibir un evento de orden creada.
 *
 * El patrón es:
 * 1. Preparar un carrito en estado EN_CHECKOUT y persistirlo.
 * 2. Publicar un evento de orden creada con ese carritoId.
 * 3. Usar Awaitility para esperar que el listener procese el evento en otro
 * hilo.
 * 4. Verificar que el estado del carrito cambió a COMPLETADO en BD.
 */
@SpringBootTest
@DisplayName("Listener: OrdenCreada (async + Awaitility)")
class OrdenCreadaListenerIntegrationTest {
  @Autowired
  private ApplicationEventPublisher eventPublisher;
  @Autowired
  private CarritoJpaRepository carritoRepository;

  /**
   * Crea, persiste y retorna un carrito en estado EN_CHECKOUT.:
   * crearCarrito -> agregarProducto -> iniciarCheckout.
   */
  private Carrito crearCarritoEnCheckout() {
    Money precio = new Money(BigDecimal.valueOf(100), "MXN");
    ProductoRef productoRef = new ProductoRef(
        ProductoId.of(UUID.randomUUID().toString()),
        "Libro prueba",
        "LBP-001");

    Carrito carrito = Carrito.crear(ClienteId.of(UUID.randomUUID().toString()));
    carrito.agregarProducto(productoRef, 1, precio);
    carrito.iniciarCheckout();
    return carritoRepository.save(carrito);
  }

  @AfterEach
  void limpiarBD() {
    carritoRepository.deleteAll();
  }

  @Test
  @DisplayName("al publicar OrdenCreadaEvent, el carrito EN_CHECKOUT pasa a COMPLETADO")
  void alPublicarOrdenCreadaEvent_carritoCompletado() {
    // Arrange: carrito en EN_CHECKOUT
    Carrito carrito = crearCarritoEnCheckout();
    UUID carritoId = carrito.getCarritoId().valor();

    // Verificación previa de estado
    assertEquals(EstadoCarrito.EN_CHECKOUT,
        carritoRepository.findById(CarritoId.of(carritoId.toString())).orElseThrow().getEstado(),
        "el carrito debe estar EN_CHECKOUT antes de publicar el evento");

    // Arrange: construir el evento
    OrdenCreadaEvent evento = new OrdenCreadaEvent(
        UUID.randomUUID(),
        Instant.now(),
        UUID.randomUUID(),
        carritoId,
        UUID.randomUUID());

    // Act: publicar el evento (el listener es @Async, se ejecuta en otro hilo)
    eventPublisher.publishEvent(evento);

    // Assert: Awaitility espera hasta que el listener @Async termine y el estado
    // cambie
    await()
        .atMost(5, TimeUnit.SECONDS)
        .pollInterval(200, TimeUnit.MILLISECONDS)
        .untilAsserted(() -> {
          Carrito carritoActualizado = carritoRepository
              .findById(CarritoId.of(carritoId.toString()))
              .orElse(null);

          assertNotNull(carritoActualizado, "El carrito debe seguir existiendo en BD");
          assertEquals(EstadoCarrito.COMPLETADO, carritoActualizado.getEstado(),
              "El estado debe haber cambiado de EN_CHECKOUT a COMPLETADO");
        });
  }

  @Test
  @DisplayName("al publicar OrdenCreadaEvent, solo el carrito correspondiente cambia a COMPLETADO")
  void alPublicarOrdenCreadaEvent_soloElCarritoCorrectoSeActualiza() {
    // Arrange: dos carritos en EN_CHECKOUT
    Carrito carritoObjetivo = crearCarritoEnCheckout();
    Carrito carritoOtro = crearCarritoEnCheckout();

    UUID idObjetivo = carritoObjetivo.getCarritoId().valor();
    UUID idOtro = carritoOtro.getCarritoId().valor();

    // Act: publicar el evento sólo para carritoObjetivo
    eventPublisher.publishEvent(new OrdenCreadaEvent(
        UUID.randomUUID(),
        Instant.now(),
        UUID.randomUUID(),
        idObjetivo, // solo este carrito debe completarse
        UUID.randomUUID()));

    // Assert: carritoObjetivo COMPLETADO; carritoOtro permanece EN_CHECKOUT
    await()
        .atMost(5, TimeUnit.SECONDS)
        .pollInterval(200, TimeUnit.MILLISECONDS)
        .untilAsserted(() -> {
          Carrito objetivo = carritoRepository
              .findById(CarritoId.of(idObjetivo.toString()))
              .orElseThrow();
          Carrito otro = carritoRepository
              .findById(CarritoId.of(idOtro.toString()))
              .orElseThrow();

          assertEquals(EstadoCarrito.COMPLETADO, objetivo.getEstado(),
              "El carrito objetivo debe estar COMPLETADO");
          assertEquals(EstadoCarrito.EN_CHECKOUT, otro.getEstado(),
              "El otro carrito NO debe haberse modificado; debe permanecer EN_CHECKOUT");
        });
  }
}
