package com.chefsitos.uamishop.ventas.api;

import com.chefsitos.uamishop.shared.domain.valueObject.ClienteId;
import com.chefsitos.uamishop.shared.domain.valueObject.Money;
import com.chefsitos.uamishop.shared.domain.valueObject.ProductoId;
import com.chefsitos.uamishop.shared.exception.BusinessRuleException;
import com.chefsitos.uamishop.shared.exception.ResourceNotFoundException;
import com.chefsitos.uamishop.ventas.api.dto.CarritoDTO;
import com.chefsitos.uamishop.ventas.domain.aggregate.Carrito;
import com.chefsitos.uamishop.ventas.domain.enumeration.EstadoCarrito;
import com.chefsitos.uamishop.ventas.domain.valueObject.ProductoRef;
import com.chefsitos.uamishop.ventas.repository.CarritoJpaRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class CarritoApiTest {

  @Autowired
  private CarritoApi carritoApi;

  @Autowired
  private CarritoJpaRepository carritoRepository;

  private Carrito crearCarritoConProducto() {
    Carrito carrito = Carrito.crear(ClienteId.of(UUID.randomUUID().toString()));
    ProductoRef productoRef = new ProductoRef(
      ProductoId.of(UUID.randomUUID().toString()),
      "MacBook Pro 16",
      "MBP-001");

    carrito.agregarProducto(
      productoRef,
      1,
      new Money(new BigDecimal("45000.00"), "MXN"));

    return carritoRepository.save(carrito);
  }

  @Test
  void obtenerCarrito_carritoExiste_devuelveDTO() {

    Carrito carrito = crearCarritoConProducto();
    UUID carritoId = carrito.getCarritoId().getValue();

    CarritoDTO resultado = carritoApi.obtenerCarrito(carritoId);

    assertNotNull(resultado);
    assertEquals(carritoId, resultado.carritoId());
    assertEquals(carrito.getClienteId().valor(), resultado.clienteId());
    assertEquals(carrito.getEstado().name(), resultado.estado());
    assertEquals(1, resultado.items().size());
    assertEquals(0, resultado.descuentos().size());
    assertEquals(
      carrito.calcularSubtotal().cantidad(),
      resultado.subtotal());
    assertEquals(
      carrito.calcularTotal().cantidad(),
      resultado.total());
    assertEquals(
      carrito.calcularTotal().moneda(),
      resultado.moneda());
  }

  @Test
  void obtenerCarrito_carritoNoExiste_lanzaExcepcion() {

    UUID idInexistente = UUID.randomUUID();

    assertThrows(ResourceNotFoundException.class,
      () -> carritoApi.obtenerCarrito(idInexistente));
  }

  // completarCheckout
  @Test
  void completarCheckout_carritoEnCheckout_devuelveDTOCompletado() {

    Carrito carrito = crearCarritoConProducto();
    carrito.iniciarCheckout();
    carritoRepository.save(carrito);

    UUID carritoId = carrito.getCarritoId().getValue();

    CarritoDTO resultado = carritoApi.completarCheckout(carritoId);

    assertNotNull(resultado);
    assertEquals(carritoId, resultado.carritoId());
    assertEquals(EstadoCarrito.COMPLETADO.name(), resultado.estado());
  }

  @Test
  void completarCheckout_carritoActivo_lanzaExcepcion() {

    Carrito carrito = crearCarritoConProducto();
    UUID carritoId = carrito.getCarritoId().getValue();

    assertThrows(BusinessRuleException.class,
      () -> carritoApi.completarCheckout(carritoId));
  }
}
