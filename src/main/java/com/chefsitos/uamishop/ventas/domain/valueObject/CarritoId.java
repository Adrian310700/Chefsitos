package com.chefsitos.uamishop.ventas.domain.valueObject;

import java.util.UUID;

import jakarta.persistence.Embeddable;

@Embeddable
public record CarritoId(UUID valor) {

  // Constructor compacto (caracteristico de los records) solo incluye
  // validaciones para asegurar que los campos no sean nulos o vacíos
  public CarritoId {
    if (valor == null) {
      throw new IllegalArgumentException("El ID del carrito no puede ser nulo");
    }
  }

  // Método para generar un ID para Carrito
  public static CarritoId generar() {
    return new CarritoId(UUID.randomUUID());
  }

  public UUID getValue() {
    return valor;
  }
}
