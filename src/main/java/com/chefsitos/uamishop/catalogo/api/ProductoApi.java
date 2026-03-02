package com.chefsitos.uamishop.catalogo.api;

import com.chefsitos.uamishop.catalogo.api.dto.ProductoDTO;

import java.util.UUID;

public interface ProductoApi {

  ProductoDTO buscarPorId(UUID id);

}
