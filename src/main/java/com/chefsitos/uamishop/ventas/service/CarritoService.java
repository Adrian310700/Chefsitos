package com.chefsitos.uamishop.ventas.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.chefsitos.uamishop.shared.exception.ResourceNotFoundException;

import com.chefsitos.uamishop.ventas.controller.dto.CarritoRequest;
import com.chefsitos.uamishop.ventas.controller.dto.CarritoResponse;
import com.chefsitos.uamishop.ventas.controller.dto.AgregarProductoRequest;
import com.chefsitos.uamishop.ventas.controller.dto.ModificarCantidadRequest;
import com.chefsitos.uamishop.shared.domain.valueObject.Money;
import com.chefsitos.uamishop.shared.domain.valueObject.ProductoId;
import com.chefsitos.uamishop.ventas.domain.aggregate.Carrito;
import com.chefsitos.uamishop.ventas.domain.enumeration.EstadoCarrito;
import com.chefsitos.uamishop.ventas.domain.valueObject.CarritoId;
import com.chefsitos.uamishop.shared.domain.valueObject.ClienteId;
import com.chefsitos.uamishop.ventas.domain.valueObject.ProductoRef;
import com.chefsitos.uamishop.ventas.repository.CarritoJpaRepository;
import com.chefsitos.uamishop.catalogo.service.ProductoService;
import com.chefsitos.uamishop.catalogo.controller.dto.ProductoResponse;

import jakarta.transaction.Transactional;

@Service
public class CarritoService {

  private final CarritoJpaRepository carritoRepository;
  private final ProductoService productoService;

  public CarritoService(CarritoJpaRepository carritoRepository, ProductoService productoService) {
    this.carritoRepository = carritoRepository;
    this.productoService = productoService;
  }

  // Metodo SOLO para obtener el carrito por ID en el servicio de ordenes
  public Carrito obtenerCarrito(CarritoId carritoId) {
    return buscarCarrito(carritoId);
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
  public CarritoResponse obtenerCarrito(UUID carritoId) {
    Carrito carrito = buscarCarrito(CarritoId.of(carritoId.toString()));
    CarritoResponse carritoResponse = CarritoResponse.from(carrito);
    return carritoResponse;

  }

  @Transactional
  public CarritoResponse agregarProducto(UUID carritoId, AgregarProductoRequest request) {
    Carrito carrito = buscarCarrito(CarritoId.of(carritoId.toString()));

    ProductoResponse producto = productoService.buscarPorId(request.productoId());

    ProductoRef productoRef = new ProductoRef(
        ProductoId.of(request.productoId().toString()),
        producto.nombreProducto(),
        "DEF-000"); // sku: placeholder hasta tener campo propio

    Money precioUnitario = new Money(producto.precio(), producto.moneda());
    carrito.agregarProducto(productoRef, request.cantidad(), precioUnitario);
    return CarritoResponse.from(carritoRepository.save(carrito));
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
  public CarritoResponse completarCheckout(UUID carritoId) {
    Carrito carrito = buscarCarrito(CarritoId.of(carritoId.toString()));
    carrito.completarCheckout();
    return CarritoResponse.from(carritoRepository.save(carrito));
  }

  @Transactional
  public CarritoResponse abandonar(UUID carritoId) {
    Carrito carrito = buscarCarrito(CarritoId.of(carritoId.toString()));
    carrito.abandonar();
    return CarritoResponse.from(carritoRepository.save(carrito));
  }
}
