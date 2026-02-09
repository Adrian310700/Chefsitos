package com.chefsitos.uamishop.catalogo.service;

import java.util.List;
import java.util.UUID;

import com.chefsitos.uamishop.catalogo.controller.dto.CategoriaRequest;
import com.chefsitos.uamishop.catalogo.controller.dto.CategoriaResponse;
import com.chefsitos.uamishop.catalogo.controller.dto.ProductoRequest;
import com.chefsitos.uamishop.catalogo.controller.dto.ProductoResponse;

public class ProductoService {
  ProductoRequest crear(ProductoRequest request) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  ProductoResponse buscarPorId(UUID id) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  List<ProductoResponse> buscarTodos() {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  ProductoResponse actualizar(UUID id, ProductoRequest request) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  void activar(UUID id) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  void desactivar(UUID id) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  CategoriaResponse crearCategoria(CategoriaRequest request) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  CategoriaResponse buscarCategoriaPorId(UUID id) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  List<CategoriaResponse> buscarTodasCategorias() {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  CategoriaResponse actualizarCategoria(UUID id, CategoriaRequest request) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

}
