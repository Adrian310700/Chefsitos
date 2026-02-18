package com.chefsitos.uamishop.ordenes.domain.valueObject;

import com.chefsitos.uamishop.ordenes.domain.enumeration.EstadoOrden;
import java.time.LocalDateTime;

public record CambioEstado(
    EstadoOrden estadoAnterior,
    EstadoOrden estadoNuevo,
    LocalDateTime fechaCambio,
    String motivo,
    String usuarioResponsable) {
}
