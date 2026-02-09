package com.chefsitos.uamishop.catalogo.domain.valueObject;

import java.util.UUID;

public record ImagenId(UUID valor) {

  public static ImagenId generar() {
    return new ImagenId(UUID.randomUUID());
  }

  public static ImagenId of(String id) {
    if (id == null || id.isBlank()) {
      throw new IllegalArgumentException("El ID no puede ser nulo o vacío");
    }
    try {
      return new ImagenId(UUID.fromString(id));
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Formato de UUID inválido: " + id, e);
    }
  }
}