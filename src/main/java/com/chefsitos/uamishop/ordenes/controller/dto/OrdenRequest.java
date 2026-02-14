package com.chefsitos.uamishop.ordenes.controller.dto;

import com.chefsitos.uamishop.shared.domain.valueObject.Money;

import java.util.List;
import java.util.UUID;

public record OrdenRequest(
    String numeroOrden,
    UUID clienteId,
    String nombreDestinatario,
    String calle,
    String ciudad,
    String estado,
    String codigoPostal,
    String pais,
    String telefono,
    String instrucciones,
    List<ItemOrdenRequest> items) {

  public record ItemOrdenRequest(
      String productoId,
      String nombreProducto,
      String sku,
      int cantidad,
      Money precioUnitario) {
  }
}
