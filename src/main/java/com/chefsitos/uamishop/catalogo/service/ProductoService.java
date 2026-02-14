package com.chefsitos.uamishop.catalogo.service;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.chefsitos.uamishop.catalogo.controller.dto.CategoriaRequest;
import com.chefsitos.uamishop.catalogo.controller.dto.CategoriaResponse;
import com.chefsitos.uamishop.catalogo.controller.dto.ProductoRequest;
import com.chefsitos.uamishop.catalogo.controller.dto.ProductoResponse;
import com.chefsitos.uamishop.catalogo.domain.aggregate.Producto;
import com.chefsitos.uamishop.catalogo.domain.entity.Categoria;
import com.chefsitos.uamishop.catalogo.domain.valueObject.CategoriaId;
import com.chefsitos.uamishop.catalogo.domain.valueObject.ProductoId;
import com.chefsitos.uamishop.catalogo.repository.CategoriaJpaRepository;
import com.chefsitos.uamishop.catalogo.repository.ProductoJpaRepository;
import com.chefsitos.uamishop.shared.domain.valueObject.Money;

import jakarta.persistence.EntityNotFoundException;

@Service
public class ProductoService {

  @Autowired
  private ProductoJpaRepository productoRepository;

  @Autowired
  private CategoriaJpaRepository categoriaRepository;

  ProductoResponse crear(ProductoRequest request) {
    Categoria categoria = categoriaRepository.findById(CategoriaId.of(request.idCategoria()))
        .orElseThrow(() -> new EntityNotFoundException("Categoria no encontrada con ID: " + request.idCategoria()));

    Producto nuevoProducto = Producto.crear(
        request.nombreProducto(),
        request.descripcion(),
        new Money(request.precio(), request.moneda()),
        categoria.getCategoriaId());

    productoRepository.save(nuevoProducto);

    return ProductoResponse.from(nuevoProducto);
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
