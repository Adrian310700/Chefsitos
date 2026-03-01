package com.chefsitos.uamishop.ventas.api;

import java.util.UUID;

import com.chefsitos.uamishop.ventas.api.dto.CarritoDTO;

public interface CarritoApi {

  public CarritoDTO obtenerCarrito(UUID carritoId);

  public CarritoDTO completarCheckout(UUID carritoId);

}
