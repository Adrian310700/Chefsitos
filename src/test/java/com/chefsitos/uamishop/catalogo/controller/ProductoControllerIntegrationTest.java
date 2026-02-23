package com.chefsitos.uamishop.catalogo.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.chefsitos.uamishop.catalogo.controller.dto.ProductoPatchRequest;
import com.chefsitos.uamishop.catalogo.controller.dto.ProductoRequest;
import com.chefsitos.uamishop.catalogo.controller.dto.ProductoResponse;
import com.chefsitos.uamishop.catalogo.domain.aggregate.Producto;
import com.chefsitos.uamishop.catalogo.domain.entity.Categoria;
import com.chefsitos.uamishop.catalogo.domain.valueObject.CategoriaId;
import com.chefsitos.uamishop.catalogo.domain.valueObject.Imagen;
import com.chefsitos.uamishop.catalogo.repository.ProductoJpaRepository;
import com.chefsitos.uamishop.catalogo.repository.CategoriaJpaRepository;
import com.chefsitos.uamishop.shared.domain.valueObject.Money;
import com.chefsitos.uamishop.shared.domain.valueObject.ProductoId;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.UUID;

import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class ProductoControllerIntegrationTest {

  private static final String BASE_URL = "/api/v1/productos";

  @Autowired
  private TestRestTemplate restTemplate;

  @Autowired
  private ProductoJpaRepository productoRepository;

  @Autowired
  private CategoriaJpaRepository categoriaRepository;

  @AfterEach
  void cleanUp() {
    productoRepository.deleteAll();
    categoriaRepository.deleteAll();
  }

  @Nested
  @DisplayName("POST /api/v1/productos")
  class CreateProducto {

    @Test
    @DisplayName("crea producto y retorna 201 con Location header")
    void create_retorna201_yLocation() {
      // Preparar categoría requerida por el producto en BD de prueba
      UUID idCategoriaStr = UUID.randomUUID();
      com.chefsitos.uamishop.catalogo.domain.entity.Categoria categoria = com.chefsitos.uamishop.catalogo.domain.entity.Categoria
          .crear(
              new com.chefsitos.uamishop.catalogo.domain.valueObject.CategoriaId(idCategoriaStr),
              "Electronicos",
              "Dispositivos electronicos varios");
      categoriaRepository.save(categoria);

      ProductoRequest body = new ProductoRequest(
          "MacBook Pro 16",
          "Laptop de alto rendimiento",
          new BigDecimal("45000.00"),
          "MXN",
          idCategoriaStr.toString());
      HttpEntity<ProductoRequest> request = new HttpEntity<>(body);

      ResponseEntity<ProductoResponse> response = restTemplate.exchange(
          BASE_URL, HttpMethod.POST, request, ProductoResponse.class);

      assertEquals(HttpStatus.CREATED, response.getStatusCode());
      assertNotNull(response.getHeaders().getFirst("Location"));
      assertTrue(response.getHeaders().getFirst("Location").contains("/api/v1/productos/"));

      assertNotNull(response.getBody());
      assertNotNull(response.getBody().idProducto());
      assertEquals("MacBook Pro 16", response.getBody().nombreProducto());
      // Para BigDecimal usamos .compareTo() o instanciamos con double en test para
      // igualar escala si es necesario,
      // pero el test lo recibe de Json, probablemente double o sin trailing zeros.
      // Es mejor validar 0 en compareTo
      assertEquals(0, new BigDecimal("45000.00").compareTo(response.getBody().precio()));
      assertEquals("MXN", response.getBody().moneda());

      // Checamos en la BD que se haya creado el recurso
      ProductoId productoId = ProductoId.of(response.getBody().idProducto().toString());
      assertTrue(productoRepository.findById(productoId).isPresent());
    }
  }

  @Nested
  @DisplayName("GET /api/v1/productos/{id}")
  class GetProductoById {
    @Test
    @DisplayName("Crea un producto, lo busca en la BD por ID y retorna el mismo objeto")
    void getById_retorna200() {
      // crear categoria en base de datos de prueba
      UUID idCategoriaStr = UUID.randomUUID();
      Categoria categoria = Categoria
          .crear(
              new CategoriaId(idCategoriaStr),
              "Electronicos",
              "Dispositivos electronicos varios");
      categoriaRepository.save(categoria);

      Producto producto = Producto.crear(
          "MacBook Pro",
          "Laptop profesional",
          new Money(new BigDecimal(32000), "MXN"),
          categoria.getCategoriaId());

      productoRepository.save(producto);

      UUID productoId = producto.getProductoId().valor();

      // Hacer la consulta al endpoint
      ResponseEntity<ProductoResponse> response = restTemplate.getForEntity(
          BASE_URL + "/" + productoId,
          ProductoResponse.class);

      // confirmar codigo creado y que la respuesta no sea nula
      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertNotNull(response.getBody());
      // validar respuesta
      ProductoResponse body = response.getBody();
      // comprobar que es igual
      assertEquals(productoId, body.idProducto());
      assertEquals("MacBook Pro", body.nombreProducto());
      assertEquals("Laptop profesional", body.descripcion());
      assertEquals(0, new BigDecimal("32000.00").compareTo(body.precio()));
      assertEquals("MXN", body.moneda());
      assertEquals(categoria.getCategoriaId().valor(), body.idCategoria());
    }
  }

  @Nested
  @DisplayName("GET /api/v1/productos/{id}")
  class GetAll {
    @Test
    @DisplayName("retorna 200 y la lista de productos")
    void getAll_retorna200() {

      //
      CategoriaId categoriaId = CategoriaId.of(UUID.randomUUID().toString());

      Categoria categoria = Categoria.crear(
          categoriaId,
          "Electrónica",
          "Dispositivos electrónicos");
      categoriaRepository.save(categoria);

      Producto producto1 = Producto.crear(
          "MacBook Pro",
          "Laptop profesional",
          new Money(new BigDecimal("38000.00"), "MXN"),
          categoriaId);

      Producto producto2 = Producto.crear(
          "iPhone 15",
          "Smartphone de última generación",
          new Money(new BigDecimal("18000.00"), "MXN"),
          categoriaId);

      productoRepository.save(producto1);
      productoRepository.save(producto2);

      ResponseEntity<ProductoResponse[]> response = restTemplate.getForEntity(
          BASE_URL,
          ProductoResponse[].class);

      //
      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertNotNull(response.getBody());

      ProductoResponse[] body = response.getBody();

      assertEquals(2, body.length);

      java.util.List<ProductoResponse> productos = Arrays.asList(body);

      assertTrue(productos.stream()
          .anyMatch(p -> p.nombreProducto().equals("MacBook Pro")));

      assertTrue(productos.stream()
          .anyMatch(p -> p.nombreProducto().equals("iPhone 15")));
    }
  }

  @Nested
  @DisplayName("POST /api/v1/productos/{id}/activar")
  class ActivarProducto {

    @Test
    @DisplayName("retorna 200 y activa el producto cuando existe")
    void activar_retorna200_yProductoActivado() {

      //
      CategoriaId categoriaId = CategoriaId.of(UUID.randomUUID().toString());

      Categoria categoria = Categoria.crear(
          categoriaId,
          "Electrónica",
          "Dispositivos electrónicos");
      categoriaRepository.save(categoria);

      Producto producto = Producto.crear(
          "MacBook Pro",
          "Laptop profesional",
          new Money(new BigDecimal("32000.00"), "MXN"),
          categoriaId);
      // agregar una imagen para poder activar el producto
      producto.agregarImagen(
          Imagen.crear(
              "https://example.com/laptop.jpg",
              "Vista frontal",
              1));
      productoRepository.save(producto);

      UUID productoId = producto.getProductoId().valor();

      //
      ResponseEntity<ProductoResponse> response = restTemplate.postForEntity(
          BASE_URL + "/" + productoId + "/activar",
          null,
          ProductoResponse.class);

      //
      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertNotNull(response.getBody());

      ProductoResponse body = response.getBody();

      assertEquals(productoId, body.idProducto());
      assertTrue(body.disponible());

      // Comprobar que si se realizo el cambio en la BD
      Producto productoActualizado = productoRepository.findById(ProductoId.of(productoId.toString()))
          .orElseThrow();

      assertTrue(productoActualizado.isDisponible());
    }
  }

  @Nested
  @DisplayName("POST /api/v1/productos/{id}/desactivar")
  class DesactivarProducto {
    @Test
    @DisplayName("Crea producto, activa y lo desactiva posteriormente")
    void desactivarProducto_exito() {

      CategoriaId categoriaId = CategoriaId.of(UUID.randomUUID().toString());
      Categoria categoria = Categoria.crear(
          categoriaId,
          "Electrónica",
          "Dispositivos electrónicos");
      categoriaRepository.save(categoria);

      Producto producto = Producto.crear(
          "MacBook Pro",
          "Laptop profesional",
          new Money(new BigDecimal("32000.00"), "MXN"),
          categoriaId);

      // Agregar imagen
      producto.agregarImagen(
          Imagen.crear(
              "https://example.com/laptop.jpg",
              "Imagen laptop",
              1));

      producto.activar();

      productoRepository.save(producto);

      UUID productoId = producto.getProductoId().valor();

      //
      ResponseEntity<ProductoResponse> response = restTemplate.postForEntity(
          BASE_URL + "/" + productoId + "/desactivar",
          null,
          ProductoResponse.class);

      // Validaciones
      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertNotNull(response.getBody());
      assertFalse(response.getBody().disponible());
    }
  }

  @Nested
  @DisplayName("PATCH /api/v1/productos/{id}")
  class ActualizarProducto {

    @Test
    @DisplayName("Actualiza producto con PATCH retorna 200")
    void actualizarProducto_exito() {

      // Crear categoría original
      CategoriaId categoriaId = CategoriaId.of(UUID.randomUUID().toString());
      Categoria categoria = Categoria.crear(
          categoriaId,
          "Electrónica",
          "Dispositivos electrónicos");
      categoriaRepository.save(categoria);

      // Crear nueva categoría
      CategoriaId nuevaCategoriaId = CategoriaId.of(UUID.randomUUID().toString());
      Categoria nuevaCategoria = Categoria.crear(
          nuevaCategoriaId,
          "Computación",
          "Equipos de cómputo");
      categoriaRepository.save(nuevaCategoria);

      // Crear producto base
      Producto producto = Producto.crear(
          "MacBook Pro",
          "Laptop profesional",
          new Money(new BigDecimal("32000.00"), "MXN"),
          categoriaId);

      productoRepository.save(producto);

      //
      ProductoPatchRequest request = new ProductoPatchRequest(
          "MacBook Pro M3",
          "Laptop profesional actualizada",
          new BigDecimal("35000.00"),
          "USD",
          nuevaCategoriaId.valor().toString());

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);

      HttpEntity<ProductoPatchRequest> entity = new HttpEntity<>(request, headers);

      ResponseEntity<ProductoResponse> response = restTemplate.exchange(
          "/api/v1/productos/" + producto.getProductoId().valor(),
          HttpMethod.PATCH,
          entity,
          ProductoResponse.class);

      // Validaciones
      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertNotNull(response.getBody());

      assertEquals("MacBook Pro M3", response.getBody().nombreProducto());
      assertEquals("Laptop profesional actualizada", response.getBody().descripcion());
      assertEquals("USD", response.getBody().moneda());
      assertEquals("35000.00", response.getBody().precio().toString());
    }
  }
}
