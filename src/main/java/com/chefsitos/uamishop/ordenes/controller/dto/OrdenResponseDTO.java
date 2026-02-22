package com.chefsitos.uamishop.ordenes.controller.dto;

import com.chefsitos.uamishop.ordenes.domain.enumeration.EstadoOrden;
import java.math.BigDecimal;
import java.util.UUID;
// para no pasar orden directamente al controller


public record OrdenResponseDTO(
  UUID id,
  String numeroOrden,
  UUID clienteId,
  EstadoOrden estado,
  BigDecimal total,
  String moneda,
  String direccionResumen
) {}
