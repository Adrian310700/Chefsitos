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
import com.chefsitos.uamishop.shared.domain.valueObject.ProductoId;
import com.chefsitos.uamishop.catalogo.repository.CategoriaJpaRepository;
import com.chefsitos.uamishop.catalogo.repository.ProductoJpaRepository;
import com.chefsitos.uamishop.shared.domain.valueObject.Money;

import com.chefsitos.uamishop.shared.exception.ResourceNotFoundException;

@Service
public class ProductoService {

  @Autowired
  private ProductoJpaRepository productoRepository;

  @Autowired
  private CategoriaJpaRepository categoriaRepository;

  public ProductoResponse crear(ProductoRequest request) {
    Categoria categoria = categoriaRepository.findById(CategoriaId.of(request.idCategoria()))
        .orElseThrow(() -> new ResourceNotFoundException("Categoria no encontrada con ID: " + request.idCategoria()));

    Producto nuevoProducto = Producto.crear(
        request.nombreProducto(),
        request.descripcion(),
        new Money(request.precio(), request.moneda()),
        categoria.getCategoriaId());

    productoRepository.save(nuevoProducto);

    return ProductoResponse.from(nuevoProducto);
  }

  public ProductoResponse buscarPorId(UUID id) {
    Producto producto = productoRepository.findById(ProductoId.of(id + ""))
        .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + id));

    return ProductoResponse.from(producto);
  }

  public List<ProductoResponse> buscarTodos() {
    List<Producto> productos = productoRepository.findAll();
    return productos.stream().map(ProductoResponse::from).toList();
  }

  public ProductoResponse actualizar(UUID id, ProductoRequest request) {
    Producto producto = productoRepository.findById(ProductoId.of(id + ""))
        .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + id));

    producto.actualizarInformacion(request.nombreProducto(), request.descripcion());
    producto = productoRepository.save(producto);

    return ProductoResponse.from(producto);
  }

  public ProductoResponse activar(UUID id) {
    Producto producto = productoRepository.findById(ProductoId.of(id.toString()))
        .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + id));
    producto.activar();
    producto = productoRepository.save(producto);
    return ProductoResponse.from(producto);
  }

  public ProductoResponse desactivar(UUID id) {
    Producto producto = productoRepository.findById(ProductoId.of(id.toString()))
        .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + id));
    producto.desactivar();
    producto = productoRepository.save(producto);
    return ProductoResponse.from(producto);
  }

  public CategoriaResponse crearCategoria(CategoriaRequest request) {
    Categoria nuevaCategoria = Categoria.crear(
        CategoriaId.generar(),
        request.nombreCategoria(),
        request.descripcion());

    if (request.categoriaPadreId() != null) {
      nuevaCategoria.asignarPadre(CategoriaId.of(request.categoriaPadreId().toString()));
    }

    nuevaCategoria = categoriaRepository.save(nuevaCategoria);

    return new CategoriaResponse(
        nuevaCategoria.getCategoriaId().valor(),
        nuevaCategoria.getNombre(),
        nuevaCategoria.getDescripcion(),
        nuevaCategoria.getCategoriaPadreId() != null ? nuevaCategoria.getCategoriaPadreId().valor() : null);
  }

  public CategoriaResponse buscarCategoriaPorId(UUID id) {
    Categoria categoria = categoriaRepository.findById(CategoriaId.of(id + ""))
        .orElseThrow(() -> new ResourceNotFoundException("Categoria no encontrada con ID: " + id));

    return new CategoriaResponse(
        categoria.getCategoriaId().valor(),
        categoria.getNombre(),
        categoria.getDescripcion(),
        categoria.getCategoriaPadreId() != null ? categoria.getCategoriaPadreId().valor() : null);
  }

  public List<CategoriaResponse> buscarTodasCategorias() {
    List<Categoria> categorias = categoriaRepository.findAll();
    return categorias.stream().map(categoria -> new CategoriaResponse(
        categoria.getCategoriaId().valor(),
        categoria.getNombre(),
        categoria.getDescripcion(),
        categoria.getCategoriaPadreId() != null ? categoria.getCategoriaPadreId().valor() : null)).toList();
  }

  public CategoriaResponse actualizarCategoria(UUID id, CategoriaRequest request) {
    Categoria categoria = categoriaRepository.findById(CategoriaId.of(id + ""))
        .orElseThrow(() -> new ResourceNotFoundException("Categoria no encontrada con ID: " + id));

    categoria.actualizar(request.nombreCategoria(), request.descripcion());

    if (request.categoriaPadreId() != null) {
      categoria.asignarPadre(CategoriaId.of(request.categoriaPadreId().toString()));
    } else {
      categoria.asignarPadre(null);
    }

    categoria = categoriaRepository.save(categoria);

    return new CategoriaResponse(
        categoria.getCategoriaId().valor(),
        categoria.getNombre(),
        categoria.getDescripcion(),
        categoria.getCategoriaPadreId() != null ? categoria.getCategoriaPadreId().valor() : null);
  }

}
