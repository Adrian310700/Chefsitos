package com.chefsitos.uamishop.catalogo.domain.valueObject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas para el Value Object Imagen.
 * Valida especificamente la regla RN-CAT-07 sobre formato de URLs.
 */
@DisplayName("Dominio: Value Object - Imagen")
class ImagenTest {

  @Test
  @DisplayName("Debe crear una imagen válida con atributos correctos")
  void debeCrearImagenValida() {

    // Given
    String urlValida = "https://uamishop.com/img/producto1.jpg";
    String altText = "Vista Frontal";
    Integer orden = 1;

    // When
    Imagen imagen = new Imagen(urlValida, altText, orden);

    // Then
    assertAll("Atributos de Imagen",
        () -> assertNotNull(imagen.getId(), "La imagen debe tener ID autogenerado"),
        () -> assertEquals(urlValida, imagen.getUrl()),
        () -> assertEquals(altText, imagen.getAltText()),
        () -> assertEquals(orden, imagen.getOrden()));
  }

  @ParameterizedTest(name = "Debe aceptar URL válida: {0}")
  @ValueSource(strings = {
      "https://example.com/image.png",
      "http://static.site.com/assets/logo.jpg",
      "https://cdn.server.net/images/12345"
  })
  @DisplayName("Debe aceptar URLs con protocolo HTTP/HTTPS")
  void debeAceptarUrlsValidas(String url) {
    assertDoesNotThrow(() -> new Imagen(url, "Alt", 1));
  }

  @ParameterizedTest(name = "Debe rechazar URL inválida: {0}")
  @ValueSource(strings = {
      "ftp://example.com/file.png",
      "c:/local/image.jpg",
      "javascript:alert('hack')",
      "url-sin-protocolo.com",
      ""
  })
  @DisplayName("Debe rechazar protocolos no web o formatos malformados")
  void debeRechazarUrlsInvalidas(String url) {
    assertThrows(IllegalArgumentException.class, () -> {
      new Imagen(url, "Descripción", 1);
    }, "El dominio debe proteger contra URLs inseguras o inválidas");
  }

  @Test
  @DisplayName("Debe validar campos obligatorios")
  void debeValidarObligatorios() {
    assertThrows(IllegalArgumentException.class, () -> new Imagen("http://ok.com", null, 1));
  }
}
