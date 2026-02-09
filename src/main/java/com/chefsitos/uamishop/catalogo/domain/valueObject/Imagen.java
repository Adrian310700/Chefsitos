package com.chefsitos.uamishop.catalogo.domain.valueObject;

public class Imagen {

  private ImagenId id;
  private String url;
  private String altText;
  private Integer orden;

  public Imagen(String url, String altText, Integer orden) {
    // Validar campos obligatorios primero
    if (url == null || url.isBlank()) {
      throw new IllegalArgumentException("La URL no puede ser nula o vacía");
    }
    if (altText == null || altText.isBlank()) {
      throw new IllegalArgumentException("El texto alternativo no puede ser nulo o vacío");
    }
    if (orden == null) {
      throw new IllegalArgumentException("El orden no puede ser nulo");
    }
    // RN-CAT-07
    if (!(url.startsWith("http://") || url.startsWith("https://"))) {
      throw new IllegalArgumentException("La URL debe ser valida, empezar con http:// o https://");
    }
    this.id = ImagenId.generar();
    this.url = url.trim();
    this.altText = altText;
    this.orden = orden;
  }

  public ImagenId getId() {
    return this.id;
  }

  public String getUrl() {
    return this.url;
  }

  public String getAltText() {
    return this.altText;
  }

  public Integer getOrden() {
    return this.orden;
  }
}
