package com.chefsitos.uamishop.ventas.controller.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import com.chefsitos.uamishop.ventas.api.dto.CarritoDTO;
import com.chefsitos.uamishop.ventas.domain.aggregate.Carrito;
import com.chefsitos.uamishop.ventas.api.dto.ItemCarritoDTO;
import com.chefsitos.uamishop.ventas.api.dto.DescuentoDTO;

public record CarritoResponse(
    UUID carritoId,
    UUID clienteId,
    List<ItemCarritoDTO> items,
    List<DescuentoDTO> descuentos,
    String estado,
    BigDecimal subtotal,
    BigDecimal total,
    String moneda) {

  public static CarritoResponse from(Carrito carrito) {
    return new CarritoResponse(
        carrito.getCarritoId().getValue(),
        carrito.getClienteId().valor(),
        carrito.getItems().stream().map(ItemCarritoDTO::from).toList(),
        carrito.getDescuentos().stream().map(DescuentoDTO::from).toList(),
        carrito.getEstado().name(),
        carrito.calcularSubtotal().cantidad(),
        carrito.calcularTotal().cantidad(),
        carrito.calcularTotal().moneda());
  }

  public static CarritoResponse from(CarritoDTO carrito) {
    return new CarritoResponse(
        carrito.carritoId(),
        carrito.clienteId(),
        carrito.items(),
        carrito.descuentos(),
        carrito.estado(),
        carrito.subtotal(),
        carrito.total(),
        carrito.moneda());
  }
}
