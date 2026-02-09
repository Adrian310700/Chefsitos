package com.chefsitos.uamishop.catalogo.domain.valueObject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Dominio: Value Object - CategoriaId")
class CategoriaIdTest {

  @Test
  @DisplayName("Generar debe crear un UUID válido")
  void generarDebeCrearUUIDValido() {
    CategoriaId id = CategoriaId.generar();
    assertNotNull(id.getValue());
  }

  @Test
  @DisplayName("Debe ser igual a otro CategoriaId con el mismo valor")
  void testIgualdad() {
    UUID uuid = UUID.randomUUID();
    CategoriaId id1 = CategoriaId.of(uuid.toString());
    CategoriaId id2 = CategoriaId.of(uuid.toString());
    assertEquals(id1, id2);
  }

  @Test
  @DisplayName("Debe lanzar excepción con UUID malformado")
  void testUUIDInvalido() {
    assertThrows(IllegalArgumentException.class, () -> CategoriaId.of("invalido"));
  }
}
