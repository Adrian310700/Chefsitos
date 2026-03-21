package com.chefsitos.uamishop.catalogo.domain.valueObject;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import com.chefsitos.uamishop.shared.exception.BusinessRuleException;

@Embeddable
public record Imagen(
    @AttributeOverride(name = "valor", column = @Column(name = "id")) ImagenId id,
    String url,
    String altText,
    Integer orden) implements Serializable {

  public Imagen {
    if (url == null || url.isBlank()) {
      throw new BusinessRuleException("La URL no puede ser nula o vacía");
    }
    if (altText == null || altText.isBlank()) {
      throw new BusinessRuleException("El texto alternativo no puede ser nulo o vacío");
    }
    if (orden == null) {
      throw new BusinessRuleException("El orden no puede ser nulo");
    }
    if (!(url.startsWith("http://") || url.startsWith("https://"))) {
      throw new BusinessRuleException("La URL debe ser válida (http/https)");
    }

    url = url.trim();

    if (id == null) {
      id = ImagenId.generar();
    }

  }

  public static Imagen crear(String url, String altText, Integer orden) {
    return new Imagen(ImagenId.generar(), url, altText, orden);
  }
}
