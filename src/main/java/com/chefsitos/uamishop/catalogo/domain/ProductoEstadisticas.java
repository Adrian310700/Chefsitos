package com.chefsitos.uamishop.catalogo.domain;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@Entity
@Table(name = "producto_estadisticas")
@AllArgsConstructor
public class ProductoEstadisticas {
  @Id
  @Column(columnDefinition = "VARBINARY(16)")
  private UUID productoId;
  private long ventasTotales; // número de transacciones
  private long cantidadVendida; // unidades vendidas
  private long vecesAgregadoAlCarrito;
  private Instant ultimaVentaAt;
  private Instant ultimaAgregadoAlCarritoAt;
}
