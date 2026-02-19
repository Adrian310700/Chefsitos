package com.chefsitos.uamishop.ordenes.domain.valueObject;

import com.chefsitos.uamishop.ordenes.domain.enumeration.EstadoOrden;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.time.LocalDateTime;

// Value Object que registra un cambio de estado en la orden
@Embeddable
public record CambioEstado(
    @Enumerated(EnumType.STRING) EstadoOrden estadoAnterior,
    @Enumerated(EnumType.STRING) EstadoOrden estadoNuevo,
    LocalDateTime fecha,
    String motivo,
    String usuario) {
}
