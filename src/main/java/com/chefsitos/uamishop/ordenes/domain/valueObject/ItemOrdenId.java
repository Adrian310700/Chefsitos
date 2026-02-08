package com.chefsitos.uamishop.ordenes.domain.valueObject;

import java.util.UUID;

public record ItemOrdenId(UUID valor) {
  public static ItemOrdenId generar() {
    return new ItemOrdenId(UUID.randomUUID());
  }
}
