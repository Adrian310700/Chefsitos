package com.chefsitos.uamishop.catalogo.api;

import com.chefsitos.uamishop.catalogo.api.dto.ProductoDTO;

import java.util.UUID;

public interface CatalogoApi {

  ProductoDTO buscarPorId(UUID id);

}
