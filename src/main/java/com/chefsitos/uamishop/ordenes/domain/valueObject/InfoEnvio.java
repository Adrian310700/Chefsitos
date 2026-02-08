package com.chefsitos.uamishop.ordenes.domain.valueObject;

import java.time.LocalDateTime;

public record InfoEnvio(
  String proveedorLogistico,
  String numeroGuia,
  LocalDateTime fechaEstimadaEntrega
) {
  public String generarUrlRastreo() {
    return "https://logistica.com/track/" + numeroGuia;
  }
}
