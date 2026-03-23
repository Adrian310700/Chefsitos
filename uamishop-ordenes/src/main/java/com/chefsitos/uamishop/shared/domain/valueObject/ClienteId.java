package com.chefsitos.uamishop.shared.domain.valueObject;

import java.io.Serializable;
import java.util.UUID;

import jakarta.persistence.Embeddable;

@Embeddable
public record ClienteId(UUID valor) implements Serializable {

  public ClienteId {
    if (valor == null) {
      throw new IllegalArgumentException("El ID del cliente no puede ser nulo");
    }
  }

  public static ClienteId generar() {
    return new ClienteId(UUID.randomUUID());
  }

  public static ClienteId of(String id) {
    if (id == null || id.isBlank()) {
      throw new IllegalArgumentException("El ID no puede ser nulo o vacío");
    }
    try {
      return new ClienteId(UUID.fromString(id));
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Formato de UUID inválido: " + id, e);
    }
  }
}
