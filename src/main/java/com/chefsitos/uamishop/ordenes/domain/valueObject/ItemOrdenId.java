package com.chefsitos.uamishop.ordenes.domain.valueObject;

import java.io.Serializable;
import java.util.UUID;
import jakarta.persistence.Embeddable;

@Embeddable
public record ItemOrdenId(UUID valor) implements Serializable {
  public static ItemOrdenId generar() {
    return new ItemOrdenId(UUID.randomUUID());
  }
}
