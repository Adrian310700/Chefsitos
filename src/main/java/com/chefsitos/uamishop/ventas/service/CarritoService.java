package com.chefsitos.uamishop.ventas.service;

import com.chefsitos.uamishop.catalogo.api.ProductoApi;
import com.chefsitos.uamishop.catalogo.api.dto.ProductoDTO;
import com.chefsitos.uamishop.shared.domain.valueObject.CarritoId;
import com.chefsitos.uamishop.shared.domain.valueObject.ClienteId;
import com.chefsitos.uamishop.shared.domain.valueObject.Money;
import com.chefsitos.uamishop.shared.domain.valueObject.ProductoId;
import com.chefsitos.uamishop.shared.event.ProductoAgregadoAlCarritoEvent;
import com.chefsitos.uamishop.shared.exception.BusinessRuleException;
import com.chefsitos.uamishop.shared.exception.ResourceNotFoundException;
import com.chefsitos.uamishop.ventas.api.CarritoApi;
import com.chefsitos.uamishop.ventas.api.dto.CarritoDTO;
import com.chefsitos.uamishop.ventas.controller.dto.AgregarProductoRequest;
import com.chefsitos.uamishop.ventas.controller.dto.CarritoRequest;
import com.chefsitos.uamishop.ventas.controller.dto.CarritoResponse;
import com.chefsitos.uamishop.ventas.controller.dto.ModificarCantidadRequest;
import com.chefsitos.uamishop.ventas.domain.aggregate.Carrito;
import com.chefsitos.uamishop.ventas.domain.enumeration.EstadoCarrito;
import com.chefsitos.uamishop.ventas.domain.valueObject.ProductoRef;
import com.chefsitos.uamishop.ventas.repository.CarritoJpaRepository;
import jakarta.transaction.Transactional;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

import static com.chefsitos.uamishop.shared.util.LogColor.*;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class CarritoService implements CarritoApi {

  private final CarritoJpaRepository carritoRepository;
  private final ProductoApi productoService;
  private final ApplicationEventPublisher eventPublisher;

  public CarritoService(CarritoJpaRepository carritoRepository,
      ProductoApi productoService,
      ApplicationEventPublisher eventPublisher) {
    this.carritoRepository = carritoRepository;
    this.productoService = productoService;
    this.eventPublisher = eventPublisher;
  }

  // Método privado para buscar un carrito por ID en este servicio
  private Carrito buscarCarrito(CarritoId carritoId) {
    return carritoRepository.findById(carritoId)
        .orElseThrow(() -> new ResourceNotFoundException(
            "Carrito no encontrado con ID: " + carritoId.valor()));
  }

  @Transactional
  public CarritoResponse crear(CarritoRequest request) {
    // Buscar si el cliente ya tiene un carrito activo
    Optional<Carrito> carritoOptExistente = carritoRepository
        .findByClienteIdAndEstado(ClienteId.of(request.clienteId().toString()), EstadoCarrito.ACTIVO);

    // Si existe retornarlo
    if (carritoOptExistente.isPresent()) {
      Carrito carrito = carritoOptExistente.get();
      return CarritoResponse.from(carrito);

      // si no, crear uno nuevo
    } else {
      Carrito nuevoCarrito = Carrito.crear(ClienteId.of(request.clienteId().toString()));
      return CarritoResponse.from(carritoRepository.save(nuevoCarrito));
    }

  }

  @Transactional
  public CarritoDTO obtenerCarrito(UUID carritoId) {
    Carrito carrito = buscarCarrito(CarritoId.of(carritoId.toString()));
    return CarritoDTO.from(carrito);
  }

  @Transactional
  public CarritoResponse agregarProducto(UUID carritoId, AgregarProductoRequest request) {
    Carrito carrito = buscarCarrito(CarritoId.of(carritoId.toString()));

    ProductoDTO producto = productoService.buscarPorId(request.productoId());

    ProductoRef productoRef = new ProductoRef(
        ProductoId.of(request.productoId().toString()),
        producto.nombreProducto(),
        "DEF-000"); // sku: placeholder hasta tener campo propio

    Money precioUnitario = new Money(producto.precio(), producto.moneda());
    carrito.agregarProducto(productoRef, request.cantidad(), precioUnitario);

    Carrito carritoGuardado = carritoRepository.save(carrito);

    ProductoAgregadoAlCarritoEvent evento = new ProductoAgregadoAlCarritoEvent(
        UUID.randomUUID(),
        Instant.now(),
        request.productoId(),
        carritoGuardado.getCarritoId().valor(),
        request.cantidad(),
        producto.precio(),
        producto.moneda());
    eventPublisher.publishEvent(evento);
    log.info(AZUL + "Evento: ProductoAgregadoAlCarrito emitido" + RESET
        + " | productoId={}, carritoId={}, cantidad={}",
        request.productoId(), carritoGuardado.getCarritoId().valor(), request.cantidad());

    return CarritoResponse.from(carritoGuardado);
  }

  @Transactional
  public CarritoResponse modificarCantidad(UUID carritoId, UUID productoId,
      ModificarCantidadRequest request) {
    Carrito carrito = buscarCarrito(CarritoId.of(carritoId.toString()));
    carrito.modificarCantidad(ProductoId.of(productoId.toString()), request.nuevaCantidad());

    return CarritoResponse.from(carritoRepository.save(carrito));
  }

  @Transactional
  public CarritoResponse eliminarProducto(UUID carritoId, UUID productoId) {
    Carrito carrito = buscarCarrito(CarritoId.of(carritoId.toString()));
    carrito.eliminarProducto(ProductoId.of(productoId.toString()));

    return CarritoResponse.from(carritoRepository.save(carrito));
  }

  @Transactional
  public CarritoResponse vaciar(UUID carritoId) {
    Carrito carrito = buscarCarrito(CarritoId.of(carritoId.toString()));
    carrito.vaciar();
    return CarritoResponse.from(carritoRepository.save(carrito));
  }

  @Transactional
  public CarritoResponse iniciarCheckout(UUID carritoId) {
    Carrito carrito = buscarCarrito(CarritoId.of(carritoId.toString()));
    carrito.iniciarCheckout();
    return CarritoResponse.from(carritoRepository.save(carrito));
  }

  @Transactional
  public CarritoDTO completarCheckout(UUID carritoId) {
    Carrito carrito = buscarCarrito(CarritoId.of(carritoId.toString()));
    carrito.completarCheckout();
    return CarritoDTO.from(carritoRepository.save(carrito));
  }

  public void validarCarritoEnCheckout(UUID carritoId) {
    Carrito carrito = buscarCarrito(CarritoId.of(carritoId.toString()));
    if (carrito.getEstado() != EstadoCarrito.EN_CHECKOUT) {
      throw new BusinessRuleException("El carrito debe estar en checkout para crear una orden");
    }
  }

  @Transactional
  public CarritoResponse abandonar(UUID carritoId) {
    Carrito carrito = buscarCarrito(CarritoId.of(carritoId.toString()));
    carrito.abandonar();
    return CarritoResponse.from(carritoRepository.save(carrito));
  }
}
