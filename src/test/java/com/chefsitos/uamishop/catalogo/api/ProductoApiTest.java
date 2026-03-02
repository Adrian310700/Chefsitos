package com.chefsitos.uamishop.catalogo.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.chefsitos.uamishop.catalogo.api.dto.ProductoDTO;
import com.chefsitos.uamishop.catalogo.domain.aggregate.Producto;
import com.chefsitos.uamishop.catalogo.domain.entity.Categoria;
import com.chefsitos.uamishop.catalogo.domain.valueObject.CategoriaId;
import com.chefsitos.uamishop.catalogo.repository.CategoriaJpaRepository;
import com.chefsitos.uamishop.catalogo.repository.ProductoJpaRepository;
import com.chefsitos.uamishop.shared.domain.valueObject.Money;
import com.chefsitos.uamishop.shared.exception.ResourceNotFoundException;

import jakarta.transaction.Transactional;

@SpringBootTest
@Transactional
class ProductoApiIntegrationTest {

  @Autowired
  private ProductoApi productoApi;

  @Autowired
  private ProductoJpaRepository productoRepository;

  @Autowired
  private CategoriaJpaRepository categoriaRepository;

  private Categoria crearCategoriaEnBD(String nombre) {
    CategoriaId categoriaId = CategoriaId.of(UUID.randomUUID().toString());
    Categoria categoria = Categoria.crear(
        categoriaId,
        nombre,
        "Descripción " + nombre);
    return categoriaRepository.save(categoria);
  }

  @Test
  void buscarPorId_productoExiste_devuelveDTO() {

    Categoria categoria = crearCategoriaEnBD("Electronicos");

    Producto producto = Producto.crear(
        "MacBook Pro 16",
        "Laptop de alto rendimiento ",
        new Money(new BigDecimal("45000.00"), "MXN"),
        categoria.getCategoriaId());
    productoRepository.save(producto);

    UUID id = producto.getProductoId().valor();

    ProductoDTO resultado = productoApi.buscarPorId(id);

    assertNotNull(resultado);
    assertEquals(id, resultado.idProducto());
    assertEquals(producto.getNombre(), resultado.nombreProducto());
    assertEquals(producto.getDescripcion(), resultado.descripcion());
    assertEquals(producto.getPrecio().cantidad(), resultado.precio());
    assertEquals(producto.getPrecio().moneda(), resultado.moneda());
    assertEquals(producto.isDisponible(), resultado.disponible());
    assertEquals(producto.getCategoriaId().valor(), resultado.idCategoria());
  }

  @Test
  void buscarPorId_productoNoExiste_lanzaExcepcion() {

    UUID idInexistente = UUID.randomUUID();

    assertThrows(ResourceNotFoundException.class, () -> productoApi.buscarPorId(idInexistente));
  }
}
