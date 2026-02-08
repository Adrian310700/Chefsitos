package com.chefsitos.uamishop.ordenes.domain.valueObject;

import java.util.UUID;

public record OrdenId(UUID valor) {
  public static OrdenId generar() {
    return new OrdenId(UUID.randomUUID());
  }
}
