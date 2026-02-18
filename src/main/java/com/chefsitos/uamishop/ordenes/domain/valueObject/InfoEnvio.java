package com.chefsitos.uamishop.ordenes.domain.valueObject;

import java.time.LocalDateTime;

public record InfoEnvio(
    String proveedorLogistico,
    String numeroGuia,
    LocalDateTime fechaEstimadaEntrega) {

  public InfoEnvio {
    if (numeroGuia == null || numeroGuia.isBlank()) {
      throw new IllegalArgumentException("El número de guía es obligatorio");
    }
  }

  public String generarUrlRastreo() {
    return "https://logistica.com/track/" + numeroGuia;
  }
}
