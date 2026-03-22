package com.chefsitos.uamishop.ventas.domain.valueObject;

import java.util.UUID;

import jakarta.persistence.Embeddable;

@Embeddable
public record ItemCarritoId(UUID valor) {

  public ItemCarritoId {
    if (valor == null) {
      throw new IllegalArgumentException("ItemCarritoId no puede ser nulo");
    }
  }

  public static ItemCarritoId generar() {
    return new ItemCarritoId(UUID.randomUUID());
  }

  public UUID getValue() {
    return valor;
  }

}
