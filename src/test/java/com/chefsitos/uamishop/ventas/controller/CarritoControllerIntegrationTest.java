package com.chefsitos.uamishop.ventas.controller;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.chefsitos.uamishop.catalogo.domain.aggregate.Producto;
import com.chefsitos.uamishop.catalogo.domain.entity.Categoria;
import com.chefsitos.uamishop.catalogo.domain.valueObject.CategoriaId;
import com.chefsitos.uamishop.catalogo.repository.CategoriaJpaRepository;
import com.chefsitos.uamishop.catalogo.repository.ProductoJpaRepository;
import com.chefsitos.uamishop.shared.domain.valueObject.CarritoId;
import com.chefsitos.uamishop.shared.domain.valueObject.Money;

import com.chefsitos.uamishop.ventas.controller.dto.AgregarProductoRequest;
import com.chefsitos.uamishop.ventas.controller.dto.CarritoRequest;
import com.chefsitos.uamishop.ventas.controller.dto.CarritoResponse;
import com.chefsitos.uamishop.ventas.repository.CarritoJpaRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class CarritoControllerIntegrationTest {

  private static final String BASE_URL = "/api/v1/carritos";

  @Autowired
  private TestRestTemplate restTemplate;

  @Autowired
  private CarritoJpaRepository carritoRepository;

  @Autowired
  private ProductoJpaRepository productoRepository;

  @Autowired
  private CategoriaJpaRepository categoriaRepository;

  @AfterEach
  void cleanUp() {
    carritoRepository.deleteAll();
    productoRepository.deleteAll();
    categoriaRepository.deleteAll();
  }

  // Helpers
  private Categoria crearCategoriaEnBD(String nombre) {
    CategoriaId categoriaId = CategoriaId.of(UUID.randomUUID().toString());
    Categoria categoria = Categoria.crear(
        categoriaId,
        nombre,
        "Descripción " + nombre);
    return categoriaRepository.save(categoria);
  }

  private Producto crearProductoEnBD(String nombre, BigDecimal precio, String moneda, CategoriaId categoriaId) {
    Producto producto = Producto.crear(
        nombre,
        "Descripción " + nombre,
        new Money(precio, moneda),
        categoriaId);

    return productoRepository.save(producto);
  }

  @Nested
  @DisplayName("POST /api/v1/carrito")
  class CreateCarrito {

    @Test
    @DisplayName("crea carrito y retorna 201 con Location header")
    void crearCarrito_retorna201() {

      UUID clienteId = UUID.randomUUID();
      CarritoRequest body = new CarritoRequest(clienteId);

      HttpEntity<CarritoRequest> request = new HttpEntity<>(body);

      ResponseEntity<CarritoResponse> response = restTemplate.exchange(BASE_URL, HttpMethod.POST, request,
          CarritoResponse.class);

      assertEquals(HttpStatus.CREATED, response.getStatusCode());
      assertNotNull(response.getHeaders().getFirst("Location"));
      assertTrue(response.getHeaders().getFirst("Location").contains("/api/v1/carritos/"));

      assertNotNull(response.getBody());
      assertNotNull(response.getBody().carritoId());

      CarritoId carritoId = CarritoId.of(response.getBody().carritoId().toString());
      assertTrue(carritoRepository.findById(carritoId).isPresent());
    }
  }

  @Nested
  @DisplayName("GET /api/v1/carrito/{carritoId}")
  class GetCarrito {

    @Test
    @DisplayName("retorna carrito existente con 200")
    void getCarrito_retorna200() {
      // Crear un carrito para el test
      UUID clienteId = UUID.randomUUID();
      CarritoRequest body = new CarritoRequest(clienteId);

      HttpEntity<CarritoRequest> request = new HttpEntity<>(body);

      ResponseEntity<CarritoResponse> response = restTemplate.exchange(BASE_URL, HttpMethod.POST, request,
          CarritoResponse.class);

      UUID carritoId = response.getBody().carritoId();

      assertEquals(HttpStatus.CREATED, response.getStatusCode());

      // Obtener el carrito creado
      ResponseEntity<CarritoResponse> getResponse = restTemplate.getForEntity(BASE_URL + "/" + carritoId,
          CarritoResponse.class);

      assertEquals(HttpStatus.OK, getResponse.getStatusCode());
      assertNotNull(getResponse.getBody());
      assertEquals(carritoId, getResponse.getBody().carritoId());
    }

    @Test
    @DisplayName("retorna 404 para carrito no existente")
    void getCarrito_retorna404() {
      UUID nonExistentId = UUID.randomUUID();
      ResponseEntity<String> response = restTemplate.getForEntity(BASE_URL + "/" + nonExistentId, String.class);
      assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
  }

  @Nested
  @DisplayName("POST /api/v1/carrito/{carritoId}/productos")
  class AgregarProducto {

    @Test
    @DisplayName("agrega producto al carrito y retorna 200")
    void agregarProducto_retorna200() {

      UUID clienteId = UUID.randomUUID();
      CarritoRequest body = new CarritoRequest(clienteId);

      HttpEntity<CarritoRequest> request = new HttpEntity<>(body);

      ResponseEntity<CarritoResponse> response = restTemplate.exchange(BASE_URL, HttpMethod.POST, request,
          CarritoResponse.class);

      UUID carritoId = response.getBody().carritoId();

      Categoria categoria = crearCategoriaEnBD("Electronicos");
      Producto producto = crearProductoEnBD(
          "Producto Test",
          new BigDecimal("10.00"),
          "MXN",
          categoria.getCategoriaId());

      UUID productoId = producto.getProductoId().valor();
      int cantidad = 2;

      AgregarProductoRequest agregarProductoRequest = new AgregarProductoRequest(
          productoId,
          cantidad);
      HttpEntity<AgregarProductoRequest> agregarRequest = new HttpEntity<>(agregarProductoRequest);

      ResponseEntity<CarritoResponse> agregarResponse = restTemplate.exchange(
          BASE_URL + "/" + carritoId + "/productos",
          HttpMethod.POST,
          agregarRequest,
          CarritoResponse.class);
      assertEquals(HttpStatus.OK, agregarResponse.getStatusCode());
      assertNotNull(agregarResponse.getBody());
      assertEquals(1, agregarResponse.getBody().items().size());
      assertEquals(productoId, agregarResponse.getBody().items().get(0).productoId());
      assertEquals(cantidad, agregarResponse.getBody().items().get(0).cantidad());
      BigDecimal esperadoSubtotal = new BigDecimal("10.00").multiply(BigDecimal.valueOf(cantidad));

      assertEquals(esperadoSubtotal, agregarResponse.getBody().subtotal());
      assertEquals(esperadoSubtotal, agregarResponse.getBody().total());
      assertEquals("MXN", agregarResponse.getBody().moneda());
    }
  }
}
