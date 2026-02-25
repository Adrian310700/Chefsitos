package com.chefsitos.uamishop.catalogo.api;

import java.util.List;
import java.util.UUID;

import com.chefsitos.uamishop.catalogo.controller.dto.CategoriaRequest;
import com.chefsitos.uamishop.catalogo.controller.dto.CategoriaResponse;
import com.chefsitos.uamishop.catalogo.controller.dto.ProductoPatchRequest;
import com.chefsitos.uamishop.catalogo.controller.dto.ProductoRequest;
import com.chefsitos.uamishop.catalogo.controller.dto.ProductoResponse;

public interface CatalogoApi {
  ProductoResponse crear(ProductoRequest request);

  public ProductoResponse buscarPorId(UUID id);

  List<ProductoResponse> buscarTodos();

  ProductoResponse actualizar(UUID id, ProductoPatchRequest request);

  ProductoResponse activar(UUID id);

  public ProductoResponse desactivar(UUID id);

  public CategoriaResponse crearCategoria(CategoriaRequest request);

  public CategoriaResponse buscarCategoriaPorId(UUID id);

  List<CategoriaResponse> buscarTodasCategorias();

  CategoriaResponse actualizarCategoria(UUID id, CategoriaRequest request);
}
