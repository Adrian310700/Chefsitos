package com.chefsitos.uamishop.catalogo.domain.valueObject;

public class Imagen {

  private ImagenId id;
  private String url;
  private String altText;
  private Integer orden;

  public Imagen(String url, String altText, Integer orden) {
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
