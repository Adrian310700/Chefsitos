package com.chefsitos.uamishop.ordenes.domain.aggregate;

import com.chefsitos.uamishop.ordenes.domain.entity.ItemOrden;
import com.chefsitos.uamishop.ordenes.domain.enumeration.EstadoOrden;
import com.chefsitos.uamishop.ordenes.domain.valueObject.*;

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
 * Esta clase valida el cumplimiento de las reglas de negocio definidas
 * para el dominio de Órdenes conforme al enfoque Domain-Driven Design (DDD).
 * </p>
 *
 * <p>
 * Se prueban las reglas RN-ORD-01 a RN-ORD-09 implementadas en la clase Orden,
 * verificando la correcta creación, validación y transición de estados.
 * </p>
 *
 * <p>
 * Estas pruebas garantizan que el Aggregate Root mantenga la consistencia
 * del dominio y cumpla con las invariantes establecidas.
 * </p>
 *
 * @author Francisco
 * @version 1.0
 */
public class OrdenTest {

  /** Instancia del Aggregate Root Orden utilizada en las pruebas */
  private Orden orden;

  /** Dirección de envío válida para pruebas */
  private DireccionEnvio direccionValida;

  /** Identificador válido de cliente */
  private ClienteId clienteIdValido;

  /** Identificador válido de orden */
  private OrdenId ordenIdValido;

  /** Item de orden válido utilizado en las pruebas */
  private ItemOrden itemValido;

  /**
   * Método de inicialización ejecutado antes de cada prueba.
   *
   * <p>
   * Configura un escenario base válido que cumple con todas las reglas de negocio
   * necesarias para crear una Orden consistente.
   * </p>
   */
  @BeforeEach
  void setUp() {

    // Generación de identificador único de orden
    ordenIdValido = OrdenId.generar();

    // Creación de identificador único de cliente
    clienteIdValido = new ClienteId(UUID.randomUUID());

    /**
     * Creación de dirección válida que cumple:
     * RN-ORD-03: Código postal de 5 dígitos
     * RN-ORD-04: Teléfono de 10 dígitos
     */
    direccionValida = new DireccionEnvio(
        "Francisco",
        "Calle 1",
        "Ciudad de México",
        "CDMX",
        "01234",
        "México",
        "5512345678",
        "Casa azul");

    // Creación de objeto Money válido
    Money precio = new Money(BigDecimal.valueOf(100), "MXN");

    /**
     * Creación de ItemOrden válido que representa un producto en la orden.
     */
    itemValido = new ItemOrden(
        ItemOrdenId.generar(),
        "PROD-001",
        "Producto Test",
        "SKU-001",
        1,
        precio);

    /**
     * Creación de una Orden válida que cumple:
     *
     * RN-ORD-01: Debe tener al menos un item
     * RN-ORD-02: Total mayor a cero
     */
    orden = new Orden(
        ordenIdValido,
        "ORD-001",
        clienteIdValido,
        List.of(itemValido),
        direccionValida);
  }

  /**
   * Prueba que valida la correcta creación de una Orden.
   *
   * Valida:
   * RN-ORD-01: Orden debe tener items
   * RN-ORD-02: Total debe ser mayor a cero
   */
  @Test
  void deberiaCrearOrdenCorrectamente() {

    assertNotNull(orden);
    assertEquals(EstadoOrden.PENDIENTE, orden.getEstado());
    assertNotNull(orden.getTotal());
    assertTrue(orden.getTotal().esMayorQueCero());
  }

  /**
   * Prueba que valida que no se puede crear una Orden sin items.
   *
   * Regla validada:
   * RN-ORD-01
   */
  @Test
  void noDebeCrearOrdenSinItems() {

    assertThrows(IllegalArgumentException.class, () -> new Orden(
        OrdenId.generar(),
        "ORD-002",
        new ClienteId(UUID.randomUUID()),
        List.of(),
        direccionValida));
  }

  /**
   * Prueba que valida que el total de la orden no puede ser cero.
   *
   * Regla validada:
   * RN-ORD-02
   */
  @Test
  void noDebeCrearOrdenConTotalCero() {

    ItemOrden itemConPrecioCero = new ItemOrden(
        ItemOrdenId.generar(),
        "PROD-002",
        "Producto Cero",
        "SKU-002",
        1,
        new Money(BigDecimal.ZERO, "MXN"));

    assertThrows(IllegalArgumentException.class, () -> new Orden(
        OrdenId.generar(),
        "ORD-003",
        new ClienteId(UUID.randomUUID()),
        List.of(itemConPrecioCero),
        direccionValida));
  }

  /**
   * Prueba que valida que el código postal debe tener 5 dígitos.
   *
   * Regla validada:
   * RN-ORD-03
   */
  @Test
  void noDebeCrearDireccionConCodigoPostalInvalido() {

    assertThrows(IllegalArgumentException.class, () -> new DireccionEnvio(
        "Francisco",
        "Calle 1",
        "CDMX",
        "CDMX",
        "123",
        "México",
        "5512345678",
        "Casa"));
  }

  /**
   * Prueba que valida que el teléfono debe tener 10 dígitos.
   *
   * Regla validada:
   * RN-ORD-04
   */
  @Test
  void noDebeCrearDireccionConTelefonoInvalido() {

    assertThrows(IllegalArgumentException.class, () -> new DireccionEnvio(
        "Francisco",
        "Calle 1",
        "CDMX",
        "CDMX",
        "01234",
        "México",
        "55123",
        "Casa"));
  }

  /**
   * Prueba que valida la confirmación de la orden.
   *
   * Regla validada:
   * RN-ORD-05
   */
  @Test
  void deberiaConfirmarOrden() {

    orden.confirmar();
    assertEquals(EstadoOrden.CONFIRMADA, orden.getEstado());
  }

  /**
   * Prueba que valida que no se puede confirmar una orden ya confirmada.
   *
   * Regla validada:
   * RN-ORD-05
   */
  @Test
  void noDebeConfirmarOrdenSiNoEstaPendiente() {

    orden.confirmar();
    assertThrows(IllegalStateException.class, orden::confirmar);
  }

  /**
   * Prueba que valida el procesamiento correcto del pago.
   *
   * Reglas validadas:
   * RN-ORD-07
   * RN-ORD-08
   */
  @Test
  void deberiaProcesarPago() {

    orden.confirmar();
    orden.procesarPago("REF123456");

    assertEquals(EstadoOrden.PAGO_PROCESADO, orden.getEstado());
  }

  /**
   * Prueba que valida que no se puede procesar pago sin confirmar.
   *
   * Regla validada:
   * RN-ORD-07
   */
  @Test
  void noDebeProcesarPagoSinConfirmar() {

    assertThrows(IllegalStateException.class, () -> orden.procesarPago("REF123"));
  }

  /**
   * Prueba que valida que la referencia de pago no puede estar vacía.
   *
   * Regla validada:
   * RN-ORD-08
   */
  @Test
  void noDebeProcesarPagoConReferenciaVacia() {

    orden.confirmar();

    assertThrows(IllegalArgumentException.class,
        () -> orden.procesarPago("   "));
  }

  /**
   * Prueba que valida la transición al estado EN_PREPARACION.
   *
   * Regla validada:
   * RN-ORD-09
   */
  @Test
  void deberiaMarcarEnProceso() {

    orden.confirmar();
    orden.procesarPago("REF123");
    orden.marcarEnProceso();

    assertEquals(EstadoOrden.EN_PREPARACION, orden.getEstado());
  }

  /**
   * Prueba que valida que no se puede marcar en proceso sin pago procesado.
   *
   * Regla validada:
   * RN-ORD-09
   */
  @Test
  void noDebeMarcarEnProcesoSiPagoNoProcesado() {

    orden.confirmar();

    assertThrows(IllegalStateException.class,
        orden::marcarEnProceso);
  }

  /**
   * Prueba que valida la cancelación de una orden válida.
   *
   * Regla validada:
   * RN-ORD-14
   */
  @Test
  void deberiaCancelarSiNoEstaEnviadaNiEntregada() {

    orden.cancelar("Cancelación por el cliente");

    assertEquals(EstadoOrden.CANCELADA, orden.getEstado());
  }
}
