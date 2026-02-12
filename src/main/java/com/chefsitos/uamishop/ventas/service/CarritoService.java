package com.chefsitos.uamishop.ventas.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;

import com.chefsitos.uamishop.ventas.domain.aggregate.Carrito;
import com.chefsitos.uamishop.ventas.domain.valueObject.CarritoId;
import com.chefsitos.uamishop.ventas.domain.valueObject.ClienteId;
import com.chefsitos.uamishop.ventas.repository.CarritoRepository;

import jakarta.transaction.Transactional;

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

  @Transactional
  public Carrito vaciar(CarritoId carritoId) {

    // Validamos que el carrito exista
    Carrito carrito = carritoRepository.findById(carritoId)
        .orElseThrow(() -> new IllegalArgumentException("Carrito inexistente"));

    // Vaciamos el carrito
    carrito.vaciar();

    // Guardamos
    return carritoRepository.save(carrito);
  }

  @Transactional
  public Carrito iniciarCheckout(CarritoId carritoId) {

    // Validamos que el carrito exista
    Carrito carrito = carritoRepository.findById(carritoId)
        .orElseThrow(() -> new IllegalArgumentException("Carrito inexistente"));

    // Iniciamos checkout
    carrito.iniciarCheckout();

    // Guardamos
    return carritoRepository.save(carrito);
  }

  @Transactional
  public Carrito completarCheckout(CarritoId carritoId) {

    // Validamos que el carrito exista
    Carrito carrito = carritoRepository.findById(carritoId)
        .orElseThrow(() -> new IllegalArgumentException("Carrito inexistente"));

    // Completamos Checkout
    carrito.completarCheckout();

    // Guardamos
    return carritoRepository.save(carrito);
  }

  @Transactional
  public Carrito abandonar(CarritoId carritoId) {

    // Validamos que el carrito exista
    Carrito carrito = carritoRepository.findById(carritoId)
        .orElseThrow(() -> new IllegalArgumentException("Carrito inexistente"));

    // Abandonamos
    carrito.abandonar();

    // Guardamos
    return carritoRepository.save(carrito);
  }
}
