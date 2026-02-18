package com.chefsitos.uamishop.ordenes.domain.valueObject;

import com.chefsitos.uamishop.ordenes.domain.enumeration.EstadoPago;
import jakarta.persistence.Embeddable;
import java.time.LocalDateTime;

@Embeddable
public record ResumenPago(
    String metodoPago,
    String referenciaExterna,
    EstadoPago estadoPago,
    LocalDateTime fechaProcesamiento) {
}
