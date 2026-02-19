package com.chefsitos.uamishop.ordenes.domain.aggregate;

import com.chefsitos.uamishop.ordenes.domain.entity.ItemOrden;
import com.chefsitos.uamishop.ordenes.domain.enumeration.EstadoOrden;
import com.chefsitos.uamishop.ordenes.domain.valueObject.*;
import com.chefsitos.uamishop.shared.domain.valueObject.ClienteId;
import com.chefsitos.uamishop.shared.domain.valueObject.Money;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Clase de pruebas unitarias para el Aggregate Root {@link Orden}.
 *
 * <p>
 * Valida el cumplimiento de las reglas de negocio definidas
 * para el dominio de Órdenes conforme al enfoque Domain-Driven Design (DDD).
 * </p>
 *
 * <p>
 * Se prueban las reglas RN-ORD-01 a RN-ORD-14 implementadas en la clase Orden,
 * verificando la correcta creación, validación y transición de estados.
 * </p>
 *
 * @author Francisco
 * @version 2.0
 */
public class OrdenTest {

  private Orden orden;
  private DireccionEnvio direccionValida;
  private ClienteId clienteIdValido;
  private ItemOrden itemValido;

  @BeforeEach
  void setUp() {
    clienteIdValido = new ClienteId(UUID.randomUUID());

    // RN-VO-03 y RN-VO-04: Dirección válida con país = "México"
    direccionValida = new DireccionEnvio(
        "Francisco",
        "Calle 1",
        "Ciudad de México",
        "CDMX",
        "01234",
        "México",
        "5512345678",
        "Casa azul");

    Money precio = new Money(BigDecimal.valueOf(100), "MXN");

    itemValido = new ItemOrden(
        ItemOrdenId.generar(),
        UUID.randomUUID().toString(),
        "Producto Test",
        "SKU-001",
        1,
        precio);

    // Usar el método fábrica Orden.crear() — única forma de construir una Orden
    orden = Orden.crear(clienteIdValido, List.of(itemValido), direccionValida);
  }

  // === RN-ORD-01 y RN-ORD-02 ===

  @Test
  void deberiaCrearOrdenCorrectamente() {
    assertNotNull(orden);
    assertEquals(EstadoOrden.PENDIENTE, orden.getEstado());
    assertNotNull(orden.getTotal());
    assertTrue(orden.getTotal().esMayorQueCero());
  }

  @Test
  void noDebeCrearOrdenSinItems() {
    assertThrows(IllegalArgumentException.class, () -> Orden.crear(
        new ClienteId(UUID.randomUUID()),
        List.of(),
        direccionValida));
  }

  @Test
  void noDebeCrearOrdenConTotalCero() {
    ItemOrden itemConPrecioCero = new ItemOrden(
        ItemOrdenId.generar(),
        UUID.randomUUID().toString(),
        "Producto Cero",
        "SKU-002",
        1,
        new Money(BigDecimal.ZERO, "MXN"));

    assertThrows(IllegalArgumentException.class, () -> Orden.crear(
        new ClienteId(UUID.randomUUID()),
        List.of(itemConPrecioCero),
        direccionValida));
  }

  // === RN-ORD-03 y RN-ORD-04 ===

  @Test
  void noDebeCrearDireccionConCodigoPostalInvalido() {
    assertThrows(IllegalArgumentException.class, () -> new DireccionEnvio(
        "Francisco",
        "Calle 1",
        "Ciudad de México",
        "CDMX",
        "123",
        "México",
        "5512345678",
        "Casa"));
  }

  @Test
  void noDebeCrearDireccionConTelefonoInvalido() {
    assertThrows(IllegalArgumentException.class, () -> new DireccionEnvio(
        "Francisco",
        "Calle 1",
        "Ciudad de México",
        "CDMX",
        "01234",
        "México",
        "55123",
        "Casa"));
  }

  // === RN-VO-03: Campos obligatorios ===

  @Test
  void noDebeCrearDireccionSinCalle() {
    assertThrows(IllegalArgumentException.class, () -> new DireccionEnvio(
        "Francisco",
        "",
        "Ciudad de México",
        "CDMX",
        "01234",
        "México",
        "5512345678",
        "Casa"));
  }

  @Test
  void noDebeCrearDireccionSinCiudad() {
    assertThrows(IllegalArgumentException.class, () -> new DireccionEnvio(
        "Francisco",
        "Calle 1",
        "",
        "CDMX",
        "01234",
        "México",
        "5512345678",
        "Casa"));
  }

  // === RN-VO-04: País debe ser "México" ===

  @Test
  void noDebeCrearDireccionConPaisInvalido() {
    assertThrows(IllegalArgumentException.class, () -> new DireccionEnvio(
        "Francisco",
        "Calle 1",
        "Ciudad de México",
        "CDMX",
        "01234",
        "USA",
        "5512345678",
        "Casa"));
  }

  // === RN-ORD-05 ===

  @Test
  void deberiaConfirmarOrden() {
    orden.confirmar();
    assertEquals(EstadoOrden.CONFIRMADA, orden.getEstado());
  }

  @Test
  void noDebeConfirmarOrdenSiNoEstaPendiente() {
    orden.confirmar();
    assertThrows(IllegalStateException.class, orden::confirmar);
  }

  // === RN-ORD-07 y RN-ORD-08 ===

  @Test
  void deberiaProcesarPago() {
    orden.confirmar();
    orden.procesarPago("REF123456");
    assertEquals(EstadoOrden.PAGO_PROCESADO, orden.getEstado());
  }

  @Test
  void noDebeProcesarPagoSinConfirmar() {
    assertThrows(IllegalStateException.class, () -> orden.procesarPago("REF123"));
  }

  @Test
  void noDebeProcesarPagoConReferenciaVacia() {
    orden.confirmar();
    assertThrows(IllegalArgumentException.class,
        () -> orden.procesarPago("   "));
  }

  // === RN-ORD-09 ===

  @Test
  void deberiaMarcarEnProceso() {
    orden.confirmar();
    orden.procesarPago("REF123");
    orden.marcarEnProceso();
    assertEquals(EstadoOrden.EN_PREPARACION, orden.getEstado());
  }

  @Test
  void noDebeMarcarEnProcesoSiPagoNoProcesado() {
    orden.confirmar();
    assertThrows(IllegalStateException.class,
        orden::marcarEnProceso);
  }

  // === RN-ORD-13: EN_TRANSITO ===

  @Test
  void deberiaMarcarEnTransito() {
    orden.confirmar();
    orden.procesarPago("REF123");
    orden.marcarEnProceso();
    orden.marcarEnviada("1234567890", "DHL");
    orden.marcarEnTransito();
    assertEquals(EstadoOrden.EN_TRANSITO, orden.getEstado());
  }

  @Test
  void noDebeMarcarEnTransitoSiNoEstaEnviada() {
    orden.confirmar();
    orden.procesarPago("REF123");
    orden.marcarEnProceso();
    assertThrows(IllegalStateException.class, orden::marcarEnTransito);
  }

  // === RN-ORD-13: Entregada desde ENVIADA o EN_TRANSITO ===

  @Test
  void deberiaEntregarDesdeEnviada() {
    orden.confirmar();
    orden.procesarPago("REF123");
    orden.marcarEnProceso();
    orden.marcarEnviada("1234567890", "DHL");
    orden.marcarEntregada();
    assertEquals(EstadoOrden.ENTREGADA, orden.getEstado());
  }

  @Test
  void deberiaEntregarDesdeEnTransito() {
    orden.confirmar();
    orden.procesarPago("REF123");
    orden.marcarEnProceso();
    orden.marcarEnviada("1234567890", "DHL");
    orden.marcarEnTransito();
    orden.marcarEntregada();
    assertEquals(EstadoOrden.ENTREGADA, orden.getEstado());
  }

  // === RN-ORD-14 ===

  @Test
  void deberiaCancelarSiNoEstaEnviadaNiEntregada() {
    orden.cancelar("Cancelación por el cliente");
    assertEquals(EstadoOrden.CANCELADA, orden.getEstado());
  }

  @Test
  void noDebeCancelarSiEstaEnviada() {
    orden.confirmar();
    orden.procesarPago("REF123");
    orden.marcarEnProceso();
    orden.marcarEnviada("1234567890", "DHL");
    assertThrows(IllegalStateException.class,
        () -> orden.cancelar("Quiero cancelar la orden"));
  }

  @Test
  void noDebeCancelarSiEstaEnTransito() {
    orden.confirmar();
    orden.procesarPago("REF123");
    orden.marcarEnProceso();
    orden.marcarEnviada("1234567890", "DHL");
    orden.marcarEnTransito();
    assertThrows(IllegalStateException.class,
        () -> orden.cancelar("Quiero cancelar la orden"));
  }
}
