package com.chefsitos.uamishop.catalogo.domain.valueObject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Suite de pruebas para el Value Object ProductoId.
 * Verifica la identidad única y la validación de formato UUID.
 */
@DisplayName("Dominio: Value Object - ProductoId")
class ProductoIdTest {

  @Test
  @DisplayName("Debe generar un ID único con estructura UUID válida")
  void debeGenerarIdUnico() {
    // Act
    ProductoId id1 = ProductoId.generar();
    ProductoId id2 = ProductoId.generar();

    // Assert
    assertAll("Integridad de Generación",
        () -> assertNotNull(id1),
        () -> assertNotNull(id1.getValue(), "El valor interno UUID no debe ser nulo"),
        () -> assertNotEquals(id1, id2, "Dos IDs generados secuencialmente deben ser distintos"));
  }

  @Test
  @DisplayName("Debe reconstruir un ID a partir de un String UUID válido")
  void debeCrearDesdeString() {
    // Arrange
    String uuidStr = "550e8400-e29b-41d4-a716-446655440000";

    // Act
    ProductoId id = ProductoId.of(uuidStr);

    // Assert
    assertEquals(UUID.fromString(uuidStr), id.getValue());
  }

  @ParameterizedTest
  @DisplayName("Debe rechazar cadenas que no sean UUIDs válidos")
  @NullAndEmptySource
  @ValueSource(strings = { "uuid-invalido", "12345", "zzzz-zzzz", " " })
  void debeLanzarExcepcionFormatoInvalido(String invalidUuid) {
    // Assert
    assertThrows(IllegalArgumentException.class, () -> {
      ProductoId.of(invalidUuid);
    }, "Debe protegerse contra formatos UUID corruptos o inválidos");
  }

  @Test
  @DisplayName("Debe cumplir el contrato de igualdad (Equality Contract)")
  void debeVerificarIgualdad() {
    // Arrange
    UUID rawUuid = UUID.randomUUID();
    ProductoId id1 = ProductoId.of(rawUuid.toString());
    ProductoId id2 = ProductoId.of(rawUuid.toString());

    // Assert
    // En DDD, dos Value Objects son iguales si sus atributos son iguales.
    assertEquals(id1, id2, "Dos instancias con el mismo valor UUID deben ser iguales");
    assertEquals(id1.hashCode(), id2.hashCode(), "Los hashCodes deben coincidir");
  }
}
