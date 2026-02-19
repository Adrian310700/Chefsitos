package com.chefsitos.uamishop.ordenes.domain.valueObject;

import com.chefsitos.uamishop.ordenes.domain.enumeration.EstadoOrden;
import jakarta.persistence.Embeddable;
import java.time.LocalDateTime;

@Embeddable
public record CambioEstado(
    EstadoOrden estadoAnterior,
    EstadoOrden estadoNuevo,
    LocalDateTime fechaCambio,
    String motivo,
    String usuarioResponsable) {
}
