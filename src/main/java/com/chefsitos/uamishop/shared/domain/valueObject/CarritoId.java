package com.chefsitos.uamishop.shared.domain.valueObject;

import java.util.UUID;

import jakarta.persistence.Embeddable;

@Embeddable
public record CarritoId(UUID valor) {

  public CarritoId {
    if (valor == null) {
      throw new IllegalArgumentException("El ID del carrito no puede ser nulo");
    }
  }

  // Método para generar un ID para Carrito
  public static CarritoId generar() {
    return new CarritoId(UUID.randomUUID());
  }

  public static CarritoId of(String valor) {
    return new CarritoId(UUID.fromString(valor));
  }

  public UUID getValue() {
    return valor;
  }
}
