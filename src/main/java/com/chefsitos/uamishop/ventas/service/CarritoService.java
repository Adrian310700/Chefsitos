package com.chefsitos.uamishop.ventas.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.chefsitos.uamishop.ventas.domain.EstadoCarrito;
import com.chefsitos.uamishop.ventas.domain.entity.ItemCarrito;
import com.chefsitos.uamishop.ventas.domain.valueObject.CarritoId;
import com.chefsitos.uamishop.ventas.domain.valueObject.ClienteId;
import com.chefsitos.uamishop.ventas.domain.valueObject.DescuentoAplicado;
import com.chefsitos.uamishop.ventas.domain.valueObject.ItemCarritoId;
import com.chefsitos.uamishop.ventas.domain.valueObject.ProductoRef;
import com.chefsitos.uamishop.shared.domain.valueObject.Money;
import com.chefsitos.uamishop.ventas.domain.aggregate.Carrito;

public class CarritoService {

  public Carrito crear(ClienteId clienteId) {
    Carrito carrito = Carrito.crear(clienteId);
    return carrito;
  }

  public obtenerCarrito(CarritoId carritoId){


  }

}
