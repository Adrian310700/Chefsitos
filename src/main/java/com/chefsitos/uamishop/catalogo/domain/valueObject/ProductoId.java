package com.chefsitos.uamishop.catalogo.domain.valueObject;

import java.io.Serializable;
import java.util.UUID;

import jakarta.persistence.Embeddable;

@Embeddable
public record ProductoId(UUID valor) implements Serializable {

  public static ProductoId generar() {
    return new ProductoId(UUID.randomUUID());
  }

  public static ProductoId of(String id) {
    if (id == null || id.isBlank()) {
      throw new IllegalArgumentException("El ID no puede ser nulo o vacío");
    }
    try {
      return new ProductoId(UUID.fromString(id));
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Formato de UUID inválido: " + id, e);
    }
  }

  public UUID getValue() {
    return valor;
  }
}
