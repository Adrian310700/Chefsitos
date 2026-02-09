package com.chefsitos.uamishop.catalogo.domain.valueObject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@DisplayName("Dominio: Value Object - ImagenId")
class ImagenIdTest {
  @Test
  @DisplayName("Debe generar IDs únicos")
  void testGenerar() {
    assertNotEquals(ImagenId.generar(), ImagenId.generar());
  }
}
