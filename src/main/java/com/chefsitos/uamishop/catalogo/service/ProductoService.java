package com.chefsitos.uamishop.catalogo.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import lombok.AllArgsConstructor;

import com.chefsitos.uamishop.catalogo.api.dto.ProductoDTO;
import com.chefsitos.uamishop.catalogo.api.ProductoApi;
import com.chefsitos.uamishop.catalogo.controller.dto.CategoriaRequest;
import com.chefsitos.uamishop.catalogo.controller.dto.CategoriaResponse;
import com.chefsitos.uamishop.catalogo.controller.dto.ProductoRequest;
import com.chefsitos.uamishop.catalogo.domain.aggregate.Producto;
import com.chefsitos.uamishop.catalogo.domain.entity.Categoria;
import com.chefsitos.uamishop.catalogo.domain.valueObject.CategoriaId;
import com.chefsitos.uamishop.catalogo.repository.CategoriaJpaRepository;
import com.chefsitos.uamishop.catalogo.repository.ProductoJpaRepository;
import com.chefsitos.uamishop.shared.domain.valueObject.Money;
import com.chefsitos.uamishop.shared.domain.valueObject.ProductoId;
import com.chefsitos.uamishop.shared.exception.ResourceNotFoundException;

@Service
@AllArgsConstructor
public class ProductoService implements ProductoApi {

  private final ProductoJpaRepository productoRepository;
  private final CategoriaJpaRepository categoriaRepository;

  public Producto crear(ProductoRequest request) {
    Categoria categoria = categoriaRepository.findById(CategoriaId.of(request.idCategoria()))
        .orElseThrow(() -> new ResourceNotFoundException("Categoria no encontrada con ID: " + request.idCategoria()));

    Producto nuevoProducto = Producto.crear(
        request.nombreProducto(),
        request.descripcion(),
        new Money(request.precio(), request.moneda()),
        categoria.getCategoriaId());

    return productoRepository.save(nuevoProducto);
  }

  public ProductoDTO buscarPorId(UUID id) {
    Producto producto = productoRepository.findById(ProductoId.of(id + ""))
        .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + id));

    return ProductoDTO.from(producto);
  }

  public List<Producto> buscarTodos() {
    List<Producto> productos = productoRepository.findAll();
    return productos;
  }

  public Producto actualizar(UUID id, String nombreProducto, String descripcion, Money nuevoPrecio,
      CategoriaId idCategoria) {
    Producto producto = productoRepository.findById(ProductoId.of(id + ""))
        .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + id));

    if (idCategoria != null) {
      Categoria categoria = categoriaRepository.findById(idCategoria)
          .orElseThrow(() -> new ResourceNotFoundException("Categoria no encontrada con ID: " + idCategoria));
      producto.cambiarCategoria(categoria.getCategoriaId());
    }

    String nuevoNombre = nombreProducto != null ? nombreProducto : producto.getNombre();
    String nuevaDescripcion = descripcion != null ? descripcion : producto.getDescripcion();

    producto.actualizarInformacion(nuevoNombre, nuevaDescripcion);

    // Lógica para actualizar el precio
    if (nuevoPrecio.cantidad() != null && nuevoPrecio.moneda() != null) {
      producto.cambiarPrecio(nuevoPrecio);
    } else if (nuevoPrecio.cantidad() != null) {
      producto.cambiarPrecio(new Money(nuevoPrecio.cantidad(), producto.getPrecio().moneda()));
    } else if (nuevoPrecio.moneda() != null) {
      producto.cambiarPrecio(new Money(producto.getPrecio().cantidad(), nuevoPrecio.moneda()));
    }

    return productoRepository.save(producto);
  }

  public Producto activar(UUID id) {
    Producto producto = productoRepository.findById(ProductoId.of(id.toString()))
        .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + id));
    producto.activar();
    return productoRepository.save(producto);
  }

  public Producto desactivar(UUID id) {
    Producto producto = productoRepository.findById(ProductoId.of(id.toString()))
        .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + id));
    producto.desactivar();
    return productoRepository.save(producto);
  }

  public CategoriaResponse crearCategoria(CategoriaRequest request) {
    Categoria nuevaCategoria = Categoria.crear(
        CategoriaId.generar(),
        request.nombreCategoria(),
        request.descripcion());

    if (request.categoriaPadreId() != null) {
      categoriaRepository.findById(CategoriaId.of(request.categoriaPadreId().toString()))
          .orElseThrow(() -> new ResourceNotFoundException(
              "Categoría padre no encontrada con ID: " + request.categoriaPadreId()));
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
