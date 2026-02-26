package com.chefsitos.uamishop.catalogo.api;

import java.util.UUID;

import com.chefsitos.uamishop.catalogo.api.dto.ProductoDTO;

public interface ProductoApi {

  public ProductoDTO buscarPorId(UUID id);

}
