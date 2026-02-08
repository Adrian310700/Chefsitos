package com.chefsitos.uamishop.ordenes.domain.valueObject;

import java.util.UUID;

public record ClienteId(UUID valor) {
  public static ClienteId of(String id) {
    return new ClienteId(UUID.fromString(id));
  }
}
