package com.chefsitos.uamishop.ventas.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;

import com.chefsitos.uamishop.ventas.domain.aggregate.Carrito;
import com.chefsitos.uamishop.ventas.domain.valueObject.CarritoId;
import com.chefsitos.uamishop.ventas.domain.valueObject.ClienteId;
import com.chefsitos.uamishop.ventas.repository.CarritoRepository;

public class CarritoService {

  @Autowired
  CarritoRepository carritoRepository;

  public Carrito crear(ClienteId clienteId) {
    Carrito carrito = Carrito.crear(clienteId);
    return carrito;
  }

  public Carrito obtenerCarrito(CarritoId carritoId) {

    Optional<Carrito> carrito = carritoRepository.findById(carritoId);

    return carrito.orElseThrow(() -> new IllegalArgumentException("Carrito inexistente"));

  }

  public Carrito vaciar(CarritoId carritoId) {

    Carrito carrito = carritoRepository.findById(carritoId).orElse(null);
    // Validamos que el carrito exista
    if (carrito == null) {
      throw new IllegalArgumentException("Carrito inexistente");
    }

    // Vaciamos el carrito
    carrito.vaciar();

    // Guardamos
    carritoRepository.save(carrito);

    return carrito;
  }

  public Carrito iniciarCheckout(CarritoId carritoId) {

    Carrito carrito = carritoRepository.findById(carritoId).orElse(null);
    // Validamos que el carrito exista
    if (carrito == null) {
      throw new IllegalArgumentException("Carrito inexistente");
    }

    // Iniciamos checkout
    carrito.iniciarCheckout();

    // Guardamo
    carritoRepository.save(carrito);

    return carrito;
  }

  public Carrito completarCheckout(CarritoId carritoId) {

    Carrito carrito = carritoRepository.findById(carritoId).orElse(null);
    // Validamos que el carrito exista
    if (carrito == null) {
      throw new IllegalArgumentException("Carrito inexistente");
    }

    // Completamos Checkout
    carrito.completarCheckout();

    // Guardamos
    carritoRepository.save(carrito);

    return carrito;
  }

  public Carrito abandonar(CarritoId carritoId) {

    Carrito carrito = carritoRepository.findById(carritoId).orElse(null);
    // Validamos que el carrito exista
    if (carrito == null) {
      throw new IllegalArgumentException("Carrito inexistente");
    }

    // Abandonamos
    carrito.abandonar();

    // Guardamos
    carritoRepository.save(carrito);

    return carrito;
  }
}
