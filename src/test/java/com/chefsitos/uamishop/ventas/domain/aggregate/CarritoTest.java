package com.chefsitos.uamishop.ventas.domain.aggregate;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.chefsitos.uamishop.shared.domain.valueObject.Money;
import com.chefsitos.uamishop.shared.domain.valueObject.ProductoId;
import com.chefsitos.uamishop.ventas.domain.enumeration.TipoDescuento;
import com.chefsitos.uamishop.ventas.domain.valueObject.ClienteId;
import com.chefsitos.uamishop.ventas.domain.valueObject.DescuentoAplicado;
import com.chefsitos.uamishop.ventas.domain.valueObject.ProductoRef;

import java.util.UUID;

class CarritoTest {

  private Carrito carrito;
  private ProductoRef producto1;
  private ProductoRef producto2;
  private Money precio10;
  private Money precio20;

  @BeforeEach
  void setUp() {
    carrito = Carrito.crear(new ClienteId(UUID.randomUUID()));

    // ProductoRef ahora usa ProductoId y SKU con formato AAA-000
    producto1 = new ProductoRef(ProductoId.generar(), "P1", "AAA-001");
    producto2 = new ProductoRef(ProductoId.generar(), "P2", "BBB-002");

    precio10 = new Money(BigDecimal.valueOf(10), "MXN");
    precio20 = new Money(BigDecimal.valueOf(20), "MXN");
  }

  @Test
  void agregarProductoValido() {
    carrito.agregarProducto(producto1, 5, precio10);
    assertEquals(1, carrito.obtenerCantidadItems());
    assertEquals(new Money(BigDecimal.valueOf(50), "MXN"), carrito.calcularSubtotal());
  }

  @Test
  void noPermiteCantidadInvalida() {
    // RN-VEN-01
    assertThrows(IllegalArgumentException.class,
        () -> carrito.agregarProducto(producto1, 0, precio10));

    assertThrows(IllegalArgumentException.class,
        () -> carrito.agregarProducto(producto1, -1, precio10));
    // RN-VEN-02
    assertThrows(IllegalArgumentException.class,
        () -> carrito.agregarProducto(producto1, 11, precio10));
  }

  // RN-VEN-04
  @Test
  void sumarCantidadProductoExistente() {
    carrito.agregarProducto(producto1, 3, precio10);
    carrito.agregarProducto(producto1, 2, precio10);

    assertEquals(1, carrito.obtenerCantidadItems());
    assertEquals(new Money(BigDecimal.valueOf(50), "MXN"), carrito.calcularSubtotal());
  }

  // RN-VEN-03
  @Test
  void noPermiteMasDe20ProductosDiferentes() {
    for (int i = 0; i < 20; i++) {
      // SKUs en formato AAA-000
      String sku = String.format("%c%c%c-%03d", 'A' + (i / 26), 'A' + (i % 26), 'A', i);
      carrito.agregarProducto(
          new ProductoRef(ProductoId.generar(), "P" + i, sku),
          1,
          precio10);
    }

    assertThrows(IllegalStateException.class,
        () -> carrito.agregarProducto(producto1, 1, precio10));
  }

  // RN-VEN-05
  @Test
  void modificarCantidadValida() {
    carrito.agregarProducto(producto1, 3, precio10);
    carrito.modificarCantidad(producto1.getProductoId(), 5);

    assertEquals(new Money(BigDecimal.valueOf(50), "MXN"), carrito.calcularSubtotal());
  }

  @Test
  void modificarCantidadInvalida() {
    carrito.agregarProducto(producto1, 3, precio10);

    assertThrows(IllegalArgumentException.class,
        () -> carrito.modificarCantidad(producto1.getProductoId(), -3));

    assertThrows(IllegalArgumentException.class,
        () -> carrito.modificarCantidad(producto1.getProductoId(), 11));
  }

  @Test
  void modificarCantidadACeroEliminaProducto() {
    carrito.agregarProducto(producto1, 3, precio10);
    carrito.modificarCantidad(producto1.getProductoId(), 0);

    assertEquals(0, carrito.obtenerCantidadItems());
    assertEquals(Money.zero("MXN"), carrito.calcularSubtotal());
  }

  // RN-VEN-06
  @Test
  void modificarCantidadCarritoCheckout() {
    carrito.agregarProducto(producto1, 10, precio10);
    carrito.iniciarCheckout();

    assertThrows(IllegalStateException.class,
        () -> carrito.modificarCantidad(producto1.getProductoId(), 1));
  }

  @Test
  void eliminarProducto() {
    carrito.agregarProducto(producto1, 3, precio10);
    carrito.agregarProducto(producto2, 2, precio20);

    carrito.eliminarProducto(producto1.getProductoId());

    assertEquals(1, carrito.obtenerCantidadItems());
    assertEquals(new Money(BigDecimal.valueOf(40), "MXN"), carrito.calcularSubtotal());
  }

  // RN-VEN-07
  @Test
  void eliminarProductoCheckout() {
    carrito.agregarProducto(producto1, 10, precio10);
    carrito.iniciarCheckout();

    assertThrows(IllegalStateException.class,
        () -> carrito.eliminarProducto(producto1.getProductoId()));
  }

  // RN-VEN-08
  @Test
  void eliminarProductoInexistente() {

    assertThrows(IllegalStateException.class,
        () -> carrito.eliminarProducto(producto1.getProductoId()));
  }

  // RN-VEN-09
  @Test
  void vaciarCarritoCheckout() {
    carrito.agregarProducto(producto1, 10, precio10);
    carrito.iniciarCheckout();

    assertThrows(IllegalStateException.class,
        () -> carrito.vaciar());
  }

  // RN-VEN-10
  @Test
  void iniciarCheckoutValido() {
    carrito.agregarProducto(producto1, 6, precio10); // $60
    assertDoesNotThrow(() -> carrito.iniciarCheckout());
  }

  // RN-VEN-11
  // RN-VEN-12
  @Test
  void iniciarCheckoutInvalido() {
    assertThrows(IllegalStateException.class, carrito::iniciarCheckout);

    carrito.agregarProducto(producto1, 4, precio10); // $40
    assertThrows(IllegalStateException.class, carrito::iniciarCheckout);
  }

  // RN-VEN-13
  // RN-VEN-14
  @Test
  void completarYAbandonarCheckout() {
    carrito.agregarProducto(producto1, 6, precio10);
    carrito.iniciarCheckout();

    assertDoesNotThrow(() -> carrito.completarCheckout());
    assertThrows(IllegalStateException.class, carrito::abandonar);
  }

  // RN-VEN-15
  @Test
  void aplicarDescuentoValido() {
    carrito.agregarProducto(producto1, 10, precio10); // $100

    carrito.aplicarDescuento(DescuentoAplicado.crear(
        "DESC20",
        TipoDescuento.CUPON,
        BigDecimal.valueOf(20)));

    assertEquals(new Money(BigDecimal.valueOf(80.0), "MXN"), carrito.calcularTotal());
  }

  // RN-VEN-16
  @Test
  void noPermiteDescuentoInvalido() {
    carrito.agregarProducto(producto1, 10, precio10);

    assertThrows(IllegalArgumentException.class,
        () -> carrito.aplicarDescuento(DescuentoAplicado.crear(
            "DESC35",
            TipoDescuento.CUPON,
            BigDecimal.valueOf(35))));
  }

  @Test
  void calcularSubtotalYTotal() {
    carrito.agregarProducto(producto1, 5, precio10);
    carrito.agregarProducto(producto2, 3, precio20);

    assertEquals(new Money(BigDecimal.valueOf(110), "MXN"), carrito.calcularSubtotal());
    assertEquals(carrito.calcularSubtotal(), carrito.calcularTotal());
  }

  // RN-VEN-15: Solo un cupón permitido
  @Test
  void noPermiteDobleCupon() {
    carrito.agregarProducto(producto1, 10, precio10);

    carrito.aplicarDescuento(DescuentoAplicado.crear("CUPON1", TipoDescuento.CUPON, BigDecimal.valueOf(10)));

    assertThrows(IllegalStateException.class,
        () -> carrito.aplicarDescuento(
            DescuentoAplicado.crear("CUPON2", TipoDescuento.CUPON, BigDecimal.valueOf(15))));
  }

  // Permite promociones adicionales además del cupón
  @Test
  void permitePromocionConCupon() {
    carrito.agregarProducto(producto1, 10, precio10);

    carrito.aplicarDescuento(DescuentoAplicado.crear("CUPON1", TipoDescuento.CUPON, BigDecimal.valueOf(10)));
    carrito.aplicarDescuento(DescuentoAplicado.crear("PROMO1", TipoDescuento.PROMOCION, BigDecimal.valueOf(5)));

    assertEquals(2, carrito.getDescuentos().size());
  }
}
