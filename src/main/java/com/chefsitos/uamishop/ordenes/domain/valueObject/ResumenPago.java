package com.chefsitos.uamishop.ordenes.domain.valueObject;

import com.chefsitos.uamishop.ordenes.domain.enumeration.EstadoPago;
import java.time.LocalDateTime;

public record ResumenPago(
  String metodoPago,
  String referenciaExterna,
  EstadoPago estado,
  LocalDateTime fechaProcesamiento
) {}
