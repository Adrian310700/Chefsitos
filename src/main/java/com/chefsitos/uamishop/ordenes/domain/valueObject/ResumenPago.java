package com.chefsitos.uamishop.ordenes.domain.valueObject;

import com.chefsitos.uamishop.ordenes.domain.enumeration.EstadoPago;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.time.LocalDateTime;

// Value Object que resume la información del pago de una orden
@Embeddable
public record ResumenPago(
        String metodoPago,
        String referenciaExterna,
        @Enumerated(EnumType.STRING) EstadoPago estado,
        LocalDateTime fechaProcesamiento) {
}
