package com.chefsitos.uamishop.catalogo.domain.valueObject;

import java.util.UUID;

import com.chefsitos.uamishop.shared.exception.BadRequestException;

import jakarta.persistence.Embeddable;

@Embeddable
public record ImagenId(UUID valor) {

  public static ImagenId generar() {
    return new ImagenId(UUID.randomUUID());
  }

  public static ImagenId of(String id) {
    if (id == null || id.isBlank()) {
      throw new BadRequestException("El ID no puede ser nulo o vacío");
    }
    try {
      return new ImagenId(UUID.fromString(id));
    } catch (IllegalArgumentException e) {
      throw new BadRequestException("Formato de UUID inválido: " + id);
    }
  }
}
