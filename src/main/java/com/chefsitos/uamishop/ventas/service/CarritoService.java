package com.chefsitos.uamishop.ventas.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.chefsitos.uamishop.shared.domain.valueObject.Money;
import com.chefsitos.uamishop.ventas.domain.EstadoCarrito;
import com.chefsitos.uamishop.ventas.domain.aggregate.Carrito;
import com.chefsitos.uamishop.ventas.domain.valueObject.CarritoId;
import com.chefsitos.uamishop.ventas.domain.valueObject.ClienteId;
import com.chefsitos.uamishop.ventas.domain.valueObject.ProductoRef;
import com.chefsitos.uamishop.ventas.repository.CarritoRepository;

import jakarta.transaction.Transactional;

@Service
public class CarritoService {

  @Autowired
  private CarritoRepository carritoRepository;

  @Transactional
  public Carrito crear(ClienteId clienteId) {
    // Carrito carrito = Carrito.crear(clienteId);
    // return carritoRepository.save(carrito);

    Optional<Carrito> carritoExistente = carritoRepository
        .findByClienteIdAndEstado(clienteId, EstadoCarrito.ACTIVO);

    // Si existe retornarlo
    if (carritoExistente.isPresent()) {
      return carritoExistente.get();
      // si no, crear uno nuevo
    } else {
      Carrito nuevoCarrito = Carrito.crear(clienteId);
      return carritoRepository.save(nuevoCarrito);
    }
  }

  @Transactional
  public Carrito obtenerCarrito(CarritoId carritoId) {

    Optional<Carrito> carrito = carritoRepository.findById(carritoId);

    return carrito.orElseThrow(() -> new IllegalArgumentException("Carrito inexistente"));

  }

  @Transactional
  public Carrito agregarProducto(CarritoId carritoId, ProductoRef productoRef, int cantidad,
      Money precioUnitario) {
    Carrito carrito = obtenerCarrito(carritoId);
    carrito.agregarProducto(productoRef, cantidad, precioUnitario);

    return carritoRepository.save(carrito);
  }

  @Transactional
  public Carrito modificarCantidad(CarritoId carritoId, UUID productoId, int nuevaCantidad) {
    Carrito carrito = obtenerCarrito(carritoId);
    carrito.modificarCantidad(productoId, nuevaCantidad);

    return carritoRepository.save(carrito);
  }

  @Transactional
  public Carrito eliminarProducto(CarritoId carritoId, UUID productoId) {
    Carrito carrito = obtenerCarrito(carritoId);
    carrito.eliminarProducto(productoId);

    return carritoRepository.save(carrito);
  }

  @Transactional
  public Carrito vaciar(CarritoId carritoId) {

    // Validamos que el carrito exista
    Carrito carrito = obtenerCarrito(carritoId);

    // Vaciamos el carrito
    carrito.vaciar();

    // Guardamos
    return carritoRepository.save(carrito);
  }

  @Transactional
  public Carrito iniciarCheckout(CarritoId carritoId) {

    // Validamos que el carrito exista
    Carrito carrito = obtenerCarrito(carritoId);

    // Iniciamos checkout
    carrito.iniciarCheckout();

    // Guardamos
    return carritoRepository.save(carrito);
  }

  @Transactional
  public Carrito completarCheckout(CarritoId carritoId) {

    // Validamos que el carrito exista
    Carrito carrito = obtenerCarrito(carritoId);

    // Completamos Checkout
    carrito.completarCheckout();

    // Guardamos
    return carritoRepository.save(carrito);
  }

  @Transactional
  public Carrito abandonar(CarritoId carritoId) {

    // Validamos que el carrito exista
    Carrito carrito = obtenerCarrito(carritoId);

    // Abandonamos
    carrito.abandonar();

    // Guardamos
    return carritoRepository.save(carrito);
  }
}
