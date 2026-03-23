package com.chefsitos.uamishop.ordenes.api.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO que representa la respuesta pública del microservicio de órdenes.
 * Debe coincidir con el contrato REST expuesto por uamishop-ordenes.
 */
public record OrdenDTO(
    UUID id,
    String numeroOrden,
    UUID clienteId,
    String estado,
    BigDecimal total,
    String moneda,
    String direccionResumen) {
}
