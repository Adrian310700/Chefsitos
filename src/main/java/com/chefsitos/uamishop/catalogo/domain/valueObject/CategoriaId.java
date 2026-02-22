package com.chefsitos.uamishop.catalogo.domain.valueObject;

import java.io.Serializable;
import java.util.UUID;

import jakarta.persistence.Embeddable;
import com.chefsitos.uamishop.shared.exception.BadRequestException;

@Embeddable
public record CategoriaId(UUID valor) implements Serializable {

  public static CategoriaId generar() {
    return new CategoriaId(UUID.randomUUID());
  }

  public static CategoriaId of(String id) {
    if (id == null || id.isBlank()) {
      throw new BadRequestException("El ID no puede ser nulo o vacío");
    }
    try {
      return new CategoriaId(UUID.fromString(id));
    } catch (IllegalArgumentException e) {
      throw new BadRequestException("Formato de UUID inválido: " + id);
    }
  }

  public UUID getValue() {
    return valor;
  }
}
