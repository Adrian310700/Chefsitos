package com.chefsitos.uamishop.ordenes.domain.valueObject;

import jakarta.persistence.Embeddable;

// Value Object que representa la dirección de envío de una orden
@Embeddable
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
    // RN-VO-03: Todos los campos obligatorios deben estar presentes
    if (nombreDestinatario == null || nombreDestinatario.isBlank()) {
      throw new IllegalArgumentException("El nombre del destinatario es obligatorio");
    }
    if (calle == null || calle.isBlank()) {
      throw new IllegalArgumentException("La calle es obligatoria");
    }
    if (ciudad == null || ciudad.isBlank()) {
      throw new IllegalArgumentException("La ciudad es obligatoria");
    }
    if (estado == null || estado.isBlank()) {
      throw new IllegalArgumentException("El estado es obligatorio");
    }
    if (pais == null || pais.isBlank()) {
      throw new IllegalArgumentException("El país es obligatorio");
    }

    // RN-VO-04: El país debe ser "México"
    if (!"México".equals(pais)) {
      throw new IllegalArgumentException("El país debe ser \"México\"");
    }

    // RN-ORD-03: El código postal debe tener 5 dígitos
    if (codigoPostal == null || !codigoPostal.matches("\\d{5}")) {
      throw new IllegalArgumentException("El código postal debe tener 5 dígitos");
    }

    // RN-ORD-04: El teléfono debe tener 10 dígitos
    if (telefono == null || !telefono.matches("\\d{10}")) {
      throw new IllegalArgumentException("El teléfono debe tener 10 dígitos");
    }
  }
}
