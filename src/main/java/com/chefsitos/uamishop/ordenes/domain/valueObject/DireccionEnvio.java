package com.chefsitos.uamishop.ordenes.domain.valueObject;

import java.util.Objects;

public record DireccionEnvio(
    String nombreDestinatario,
    String calle,
    String ciudad,
    String estado,
    String codigoPostal,
    String pais,
    String telefono,
    String instrucciones) {
  public DireccionEnvio {
    Objects.requireNonNull(nombreDestinatario);

    // RN-ORD-03 El código postal debe tener 5 dígitos
    if (codigoPostal == null || !codigoPostal.matches("\\d{5}")) {
      throw new IllegalArgumentException("El código postal debe tener exactamente 5 dígitos");
    }

    // RN-ORD-04 El teléfono debe tener 10 dígitos
    if (telefono == null || !telefono.matches("\\d{10}")) {
      throw new IllegalArgumentException("El teléfono debe tener exactamente 10 dígitos");
    }
  }

  public String formatear() {
    return String.format("%s, %s, %s, %s, %s", calle, ciudad, estado, codigoPostal, pais);
  }
}
