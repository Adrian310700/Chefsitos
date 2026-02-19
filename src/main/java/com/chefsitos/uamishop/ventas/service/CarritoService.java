package com.chefsitos.uamishop.ventas.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.chefsitos.uamishop.shared.domain.valueObject.Money;
import com.chefsitos.uamishop.shared.domain.valueObject.ProductoId;
import com.chefsitos.uamishop.ventas.domain.aggregate.Carrito;
import com.chefsitos.uamishop.ventas.domain.enumeration.EstadoCarrito;
import com.chefsitos.uamishop.ventas.domain.valueObject.CarritoId;
import com.chefsitos.uamishop.ventas.domain.valueObject.ClienteId;
import com.chefsitos.uamishop.ventas.domain.valueObject.ProductoRef;
import com.chefsitos.uamishop.ventas.repository.CarritoJpaRepository;

import jakarta.transaction.Transactional;

@Service
public class CarritoService {

  @Autowired
  private CarritoJpaRepository carritoRepository;

  @Transactional
  public Carrito crear(ClienteId clienteId) {
    // Buscar si el cliente ya tiene un carrito activo
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
  public Carrito modificarCantidad(CarritoId carritoId, ProductoId productoId, int nuevaCantidad) {
    Carrito carrito = obtenerCarrito(carritoId);
    carrito.modificarCantidad(productoId, nuevaCantidad);

    return carritoRepository.save(carrito);
  }

  @Transactional
  public Carrito eliminarProducto(CarritoId carritoId, ProductoId productoId) {
    Carrito carrito = obtenerCarrito(carritoId);
    carrito.eliminarProducto(productoId);

    return carritoRepository.save(carrito);
  }

  @Transactional
  public Carrito vaciar(CarritoId carritoId) {
    Carrito carrito = obtenerCarrito(carritoId);
    carrito.vaciar();
    return carritoRepository.save(carrito);
  }

  @Transactional
  public Carrito iniciarCheckout(CarritoId carritoId) {
    Carrito carrito = obtenerCarrito(carritoId);
    carrito.iniciarCheckout();
    return carritoRepository.save(carrito);
  }

  @Transactional
  public Carrito completarCheckout(CarritoId carritoId) {
    Carrito carrito = obtenerCarrito(carritoId);
    carrito.completarCheckout();
    return carritoRepository.save(carrito);
  }

  @Transactional
  public Carrito abandonar(CarritoId carritoId) {
    Carrito carrito = obtenerCarrito(carritoId);
    carrito.abandonar();
    return carritoRepository.save(carrito);
  }
}
