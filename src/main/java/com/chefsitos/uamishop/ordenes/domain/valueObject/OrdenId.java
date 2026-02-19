package com.chefsitos.uamishop.ordenes.domain.valueObject;

import jakarta.persistence.Embeddable;
import java.util.UUID;

@Embeddable
public record OrdenId(UUID valor) {

  public static OrdenId generar() {
    return new OrdenId(UUID.randomUUID());
  }

  public UUID getValue() {
    return valor;
  }
}
