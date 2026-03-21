package com.chefsitos.uamishop.ventas.controller;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
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
import com.chefsitos.uamishop.ventas.controller.dto.ModificarCantidadRequest;
import com.chefsitos.uamishop.ventas.repository.CarritoJpaRepository;

import org.springframework.http.MediaType;

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

  private <T> HttpEntity<T> jsonRequest(T body) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    return new HttpEntity<>(body, headers);
  }

  // Helper: crea un carrito con un producto ya agregado y retorna [carritoId,
  // productoId]
  private UUID[] crearCarritoConProducto() {
    ResponseEntity<CarritoResponse> carritoResponse = restTemplate.exchange(
        BASE_URL, HttpMethod.POST, jsonRequest(new CarritoRequest(UUID.randomUUID())),
        CarritoResponse.class);
    assertEquals(HttpStatus.CREATED, carritoResponse.getStatusCode());
    UUID carritoId = carritoResponse.getBody().carritoId();

    Categoria categoria = crearCategoriaEnBD("Electronicos");
    Producto producto = crearProductoEnBD("Producto Test", new BigDecimal("10.00"), "MXN",
        categoria.getCategoriaId());
    UUID productoId = producto.getProductoId().valor();

    restTemplate.exchange(
        BASE_URL + "/" + carritoId + "/productos",
        HttpMethod.POST,
        jsonRequest(new AgregarProductoRequest(productoId, 3)),
        CarritoResponse.class);

    return new UUID[] { carritoId, productoId };
  }

  // Helper: crea un carrito en estado EN_CHECKOUT (6 x $10 = $60 >= minimo $50)
  private UUID crearCarritoEnCheckout() {
    ResponseEntity<CarritoResponse> carritoResponse = restTemplate.exchange(
        BASE_URL, HttpMethod.POST, jsonRequest(new CarritoRequest(UUID.randomUUID())),
        CarritoResponse.class);
    assertEquals(HttpStatus.CREATED, carritoResponse.getStatusCode());
    UUID carritoId = carritoResponse.getBody().carritoId();

    Categoria categoria = crearCategoriaEnBD("Electronicos");
    Producto producto = crearProductoEnBD("Producto Test", new BigDecimal("10.00"), "MXN",
        categoria.getCategoriaId());
    restTemplate.exchange(
        BASE_URL + "/" + carritoId + "/productos",
        HttpMethod.POST,
        jsonRequest(new AgregarProductoRequest(producto.getProductoId().valor(), 6)),
        CarritoResponse.class);
    restTemplate.exchange(
        BASE_URL + "/" + carritoId + "/checkout",
        HttpMethod.POST,
        HttpEntity.EMPTY,
        CarritoResponse.class);

    return carritoId;
  }

  @Nested
  @DisplayName("POST /api/v1/carritos")
  class CreateCarrito {
    @Test
    @DisplayName("crea carrito y retorna 201 con Location header")
    void crearCarrito_retorna201() {
      UUID clienteId = UUID.randomUUID();
      CarritoRequest body = new CarritoRequest(clienteId);

      ResponseEntity<CarritoResponse> response = restTemplate.exchange(
          BASE_URL, HttpMethod.POST, jsonRequest(body), CarritoResponse.class);

      assertEquals(HttpStatus.CREATED, response.getStatusCode());
      assertNotNull(response.getHeaders().getFirst("Location"));
      assertTrue(response.getHeaders().getFirst("Location").contains("/api/v1/carritos/"));

      assertNotNull(response.getBody());
      assertNotNull(response.getBody().carritoId());

      CarritoId carritoId = CarritoId.of(response.getBody().carritoId().toString());
      assertTrue(carritoRepository.findById(carritoId).isPresent());
    }

    @Test
    @DisplayName("retorna el mismo carrito con 201 si el cliente ya tiene uno activo")
    void crearCarrito_clienteYaTieneCarrito_retornaElMismoCarrito() {
      UUID clienteId = UUID.randomUUID();
      CarritoRequest body = new CarritoRequest(clienteId);

      // Primera creación — debe ser exitosa (201)
      ResponseEntity<CarritoResponse> primera = restTemplate.exchange(
          BASE_URL, HttpMethod.POST, jsonRequest(body), CarritoResponse.class);
      assertEquals(HttpStatus.CREATED, primera.getStatusCode());
      UUID carritoIdOriginal = primera.getBody().carritoId();

      // Segunda solicitud con el mismo clienteId — retorna el mismo carrito (201)
      ResponseEntity<CarritoResponse> segunda = restTemplate.exchange(
          BASE_URL, HttpMethod.POST, jsonRequest(body), CarritoResponse.class);

      assertEquals(HttpStatus.CREATED, segunda.getStatusCode());
      assertNotNull(segunda.getBody());
      assertEquals(carritoIdOriginal, segunda.getBody().carritoId());
    }
  }

  @Nested
  @DisplayName("GET /api/v1/carritos/{carritoId}")
  class GetCarrito {
    @Test
    @DisplayName("retorna carrito existente con 200")
    void getCarrito_retorna200() {
      // Crear un carrito como prerequisito del test
      UUID clienteId = UUID.randomUUID();
      CarritoRequest body = new CarritoRequest(clienteId);
      ResponseEntity<CarritoResponse> createResponse = restTemplate.exchange(
          BASE_URL, HttpMethod.POST, jsonRequest(body), CarritoResponse.class);
      assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
      UUID carritoId = createResponse.getBody().carritoId();

      // Obtener el carrito creado
      ResponseEntity<CarritoResponse> getResponse = restTemplate.getForEntity(
          BASE_URL + "/" + carritoId, CarritoResponse.class);

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
  @DisplayName("POST /api/v1/carritos/{carritoId}/productos")
  class AgregarProducto {

    @Test
    @DisplayName("agrega producto al carrito y retorna 200")
    void agregarProducto_retorna200() {
      ResponseEntity<CarritoResponse> carritoResponse = restTemplate.exchange(
          BASE_URL, HttpMethod.POST, jsonRequest(new CarritoRequest(UUID.randomUUID())), CarritoResponse.class);
      assertEquals(HttpStatus.CREATED, carritoResponse.getStatusCode());
      UUID carritoId = carritoResponse.getBody().carritoId();

      Categoria categoria = crearCategoriaEnBD("Electronicos");
      Producto producto = crearProductoEnBD(
          "Producto Test",
          new BigDecimal("10.00"),
          "MXN",
          categoria.getCategoriaId());

      UUID productoId = producto.getProductoId().valor();
      int cantidad = 2;

      ResponseEntity<CarritoResponse> agregarResponse = restTemplate.exchange(
          BASE_URL + "/" + carritoId + "/productos",
          HttpMethod.POST,
          jsonRequest(new AgregarProductoRequest(productoId, cantidad)),
          CarritoResponse.class);
      assertEquals(HttpStatus.OK, agregarResponse.getStatusCode());
      assertNotNull(agregarResponse.getBody());
      assertEquals(1, agregarResponse.getBody().items().size());
      assertEquals(productoId, agregarResponse.getBody().items().getFirst().productoId());
      assertEquals(cantidad, agregarResponse.getBody().items().getFirst().cantidad());
      BigDecimal esperadoSubtotal = new BigDecimal("10.00").multiply(BigDecimal.valueOf(cantidad));

      assertEquals(esperadoSubtotal, agregarResponse.getBody().subtotal());
      assertEquals(esperadoSubtotal, agregarResponse.getBody().total());
      assertEquals("MXN", agregarResponse.getBody().moneda());
    }

    @Test
    @DisplayName("retorna 404 si el carrito no existe")
    void agregarProducto_carritoInexistente_retorna404() {
      ResponseEntity<String> response = restTemplate.exchange(
          BASE_URL + "/" + UUID.randomUUID() + "/productos",
          HttpMethod.POST,
          jsonRequest(new AgregarProductoRequest(UUID.randomUUID(), 1)),
          String.class);

      assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("retorna 404 si el producto no existe")
    void agregarProducto_productoInexistente_retorna404() {
      ResponseEntity<CarritoResponse> carritoResponse = restTemplate.exchange(
          BASE_URL, HttpMethod.POST, jsonRequest(new CarritoRequest(UUID.randomUUID())), CarritoResponse.class);
      assertEquals(HttpStatus.CREATED, carritoResponse.getStatusCode());
      UUID carritoId = carritoResponse.getBody().carritoId();

      ResponseEntity<String> response = restTemplate.exchange(
          BASE_URL + "/" + carritoId + "/productos",
          HttpMethod.POST,
          jsonRequest(new AgregarProductoRequest(UUID.randomUUID(), 1)),
          String.class);

      assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("retorna 400 si la cantidad es negativa")
    void agregarProducto_cantidadNegativa_retorna400() {
      ResponseEntity<CarritoResponse> carritoResponse = restTemplate.exchange(
          BASE_URL, HttpMethod.POST, jsonRequest(new CarritoRequest(UUID.randomUUID())), CarritoResponse.class);
      assertEquals(HttpStatus.CREATED, carritoResponse.getStatusCode());
      UUID carritoId = carritoResponse.getBody().carritoId();

      ResponseEntity<String> response = restTemplate.exchange(
          BASE_URL + "/" + carritoId + "/productos",
          HttpMethod.POST,
          jsonRequest(new AgregarProductoRequest(UUID.randomUUID(), -1)),
          String.class);

      assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("retorna 400 si la cantidad es 0")
    void agregarProducto_cantidadCero_retorna400() {
      ResponseEntity<CarritoResponse> carritoResponse = restTemplate.exchange(
          BASE_URL, HttpMethod.POST, jsonRequest(new CarritoRequest(UUID.randomUUID())), CarritoResponse.class);
      assertEquals(HttpStatus.CREATED, carritoResponse.getStatusCode());
      UUID carritoId = carritoResponse.getBody().carritoId();

      ResponseEntity<String> response = restTemplate.exchange(
          BASE_URL + "/" + carritoId + "/productos",
          HttpMethod.POST,
          jsonRequest(new AgregarProductoRequest(UUID.randomUUID(), 0)),
          String.class);

      assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
  }

  @Nested
  @DisplayName("PATCH /api/v1/carritos/{carritoId}/productos/{productoId}")
  class ModificarCantidad {

    @Test
    @DisplayName("modifica la cantidad del producto y retorna 200")
    void modificarCantidad_retorna200() {
      UUID[] ids = crearCarritoConProducto();
      UUID carritoId = ids[0];
      UUID productoId = ids[1];

      ResponseEntity<CarritoResponse> response = restTemplate.exchange(
          BASE_URL + "/" + carritoId + "/productos/" + productoId,
          HttpMethod.PATCH,
          jsonRequest(new ModificarCantidadRequest(5)),
          CarritoResponse.class);

      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertNotNull(response.getBody());
      assertEquals(1, response.getBody().items().size());
      assertEquals(productoId, response.getBody().items().getFirst().productoId());
      assertEquals(5, response.getBody().items().getFirst().cantidad());
    }

    @Test
    @DisplayName("elimina el producto del carrito cuando la cantidad es 0")
    void modificarCantidad_cero_eliminaProducto() {
      UUID[] ids = crearCarritoConProducto();
      UUID carritoId = ids[0];
      UUID productoId = ids[1];

      ResponseEntity<CarritoResponse> response = restTemplate.exchange(
          BASE_URL + "/" + carritoId + "/productos/" + productoId,
          HttpMethod.PATCH,
          jsonRequest(new ModificarCantidadRequest(0)),
          CarritoResponse.class);

      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertNotNull(response.getBody());
      assertEquals(0, response.getBody().items().size());
    }

    @Test
    @DisplayName("retorna 404 si el carrito no existe")
    void modificarCantidad_carritoInexistente_retorna404() {
      ResponseEntity<String> response = restTemplate.exchange(
          BASE_URL + "/" + UUID.randomUUID() + "/productos/" + UUID.randomUUID(),
          HttpMethod.PATCH,
          jsonRequest(new ModificarCantidadRequest(2)),
          String.class);

      assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("retorna 422 si el producto no existe en el carrito")
    void modificarCantidad_productoNoEnCarrito_retorna422() {
      UUID[] ids = crearCarritoConProducto();
      UUID carritoId = ids[0];

      ResponseEntity<String> response = restTemplate.exchange(
          BASE_URL + "/" + carritoId + "/productos/" + UUID.randomUUID(),
          HttpMethod.PATCH,
          jsonRequest(new ModificarCantidadRequest(2)),
          String.class);

      assertEquals(HttpStatus.UNPROCESSABLE_CONTENT, response.getStatusCode());
    }

    @Test
    @DisplayName("retorna 400 si la cantidad es negativa")
    void modificarCantidad_cantidadNegativa_retorna400() {
      UUID[] ids = crearCarritoConProducto();
      UUID carritoId = ids[0];
      UUID productoId = ids[1];

      ResponseEntity<String> response = restTemplate.exchange(
          BASE_URL + "/" + carritoId + "/productos/" + productoId,
          HttpMethod.PATCH,
          jsonRequest(new ModificarCantidadRequest(-1)),
          String.class);

      assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("retorna 400 si la cantidad excede el máximo de 10")
    void modificarCantidad_cantidadExcedeLimite_retorna400() {
      UUID[] ids = crearCarritoConProducto();
      UUID carritoId = ids[0];
      UUID productoId = ids[1];

      ResponseEntity<String> response = restTemplate.exchange(
          BASE_URL + "/" + carritoId + "/productos/" + productoId,
          HttpMethod.PATCH,
          jsonRequest(new ModificarCantidadRequest(11)),
          String.class);

      assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
  }

  @Nested
  @DisplayName("DELETE /api/v1/carritos/{carritoId}/productos/{productoId}")
  class EliminarProducto {

    @Test
    @DisplayName("elimina el producto del carrito y retorna 200")
    void eliminarProducto_retorna200() {
      UUID[] ids = crearCarritoConProducto();
      UUID carritoId = ids[0];
      UUID productoId = ids[1];

      ResponseEntity<CarritoResponse> response = restTemplate.exchange(
          BASE_URL + "/" + carritoId + "/productos/" + productoId,
          HttpMethod.DELETE,
          HttpEntity.EMPTY,
          CarritoResponse.class);

      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertNotNull(response.getBody());
      assertEquals(0, response.getBody().items().size());
    }

    @Test
    @DisplayName("retorna 404 si el carrito no existe")
    void eliminarProducto_carritoInexistente_retorna404() {
      ResponseEntity<String> response = restTemplate.exchange(
          BASE_URL + "/" + UUID.randomUUID() + "/productos/" + UUID.randomUUID(),
          HttpMethod.DELETE,
          HttpEntity.EMPTY,
          String.class);

      assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("retorna 422 si el producto no existe en el carrito")
    void eliminarProducto_productoNoEnCarrito_retorna422() {
      UUID[] ids = crearCarritoConProducto();
      UUID carritoId = ids[0];

      ResponseEntity<String> response = restTemplate.exchange(
          BASE_URL + "/" + carritoId + "/productos/" + UUID.randomUUID(),
          HttpMethod.DELETE,
          HttpEntity.EMPTY,
          String.class);

      assertEquals(HttpStatus.UNPROCESSABLE_CONTENT, response.getStatusCode());
    }
  }

  @Nested
  @DisplayName("DELETE /api/v1/carritos/{carritoId}/productos")
  class VaciarCarrito {

    @Test
    @DisplayName("vacia el carrito con productos y retorna 200")
    void vaciarCarrito_retorna200() {
      UUID[] ids = crearCarritoConProducto();
      UUID carritoId = ids[0];

      ResponseEntity<CarritoResponse> response = restTemplate.exchange(
          BASE_URL + "/" + carritoId + "/productos",
          HttpMethod.DELETE,
          HttpEntity.EMPTY,
          CarritoResponse.class);

      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertNotNull(response.getBody());
      assertEquals(0, response.getBody().items().size());
    }

    @Test
    @DisplayName("vacia un carrito que ya estaba vacio y retorna 200")
    void vaciarCarrito_yaVacio_retorna200() {
      ResponseEntity<CarritoResponse> carritoResponse = restTemplate.exchange(
          BASE_URL, HttpMethod.POST, jsonRequest(new CarritoRequest(UUID.randomUUID())),
          CarritoResponse.class);
      assertEquals(HttpStatus.CREATED, carritoResponse.getStatusCode());
      UUID carritoId = carritoResponse.getBody().carritoId();

      ResponseEntity<CarritoResponse> response = restTemplate.exchange(
          BASE_URL + "/" + carritoId + "/productos",
          HttpMethod.DELETE,
          HttpEntity.EMPTY,
          CarritoResponse.class);

      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertNotNull(response.getBody());
      assertEquals(0, response.getBody().items().size());
    }

    @Test
    @DisplayName("retorna 404 si el carrito no existe")
    void vaciarCarrito_carritoInexistente_retorna404() {
      ResponseEntity<String> response = restTemplate.exchange(
          BASE_URL + "/" + UUID.randomUUID() + "/productos",
          HttpMethod.DELETE,
          HttpEntity.EMPTY,
          String.class);

      assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
  }

  @Nested
  @DisplayName("POST /api/v1/carritos/{carritoId}/checkout")
  class IniciarCheckout {

    @Test
    @DisplayName("inicia checkout y retorna 200")
    void iniciarCheckout_retorna200() {
      // Se necesita subtotal >= 50 MXN (RN-VEN-12): 6 x $10.00 = $60.00
      ResponseEntity<CarritoResponse> carritoResponse = restTemplate.exchange(
          BASE_URL, HttpMethod.POST, jsonRequest(new CarritoRequest(UUID.randomUUID())),
          CarritoResponse.class);
      assertEquals(HttpStatus.CREATED, carritoResponse.getStatusCode());
      UUID carritoId = carritoResponse.getBody().carritoId();

      Categoria categoria = crearCategoriaEnBD("Electronicos");
      Producto producto = crearProductoEnBD("Producto Test", new BigDecimal("10.00"), "MXN",
          categoria.getCategoriaId());
      restTemplate.exchange(
          BASE_URL + "/" + carritoId + "/productos",
          HttpMethod.POST,
          jsonRequest(new AgregarProductoRequest(producto.getProductoId().valor(), 6)),
          CarritoResponse.class);

      ResponseEntity<CarritoResponse> response = restTemplate.exchange(
          BASE_URL + "/" + carritoId + "/checkout",
          HttpMethod.POST,
          HttpEntity.EMPTY,
          CarritoResponse.class);

      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("retorna 422 si el carrito esta vacio (RN-VEN-10)")
    void iniciarCheckout_carritoVacio_retorna422() {
      ResponseEntity<CarritoResponse> carritoResponse = restTemplate.exchange(
          BASE_URL, HttpMethod.POST, jsonRequest(new CarritoRequest(UUID.randomUUID())),
          CarritoResponse.class);
      assertEquals(HttpStatus.CREATED, carritoResponse.getStatusCode());
      UUID carritoId = carritoResponse.getBody().carritoId();

      ResponseEntity<String> response = restTemplate.exchange(
          BASE_URL + "/" + carritoId + "/checkout",
          HttpMethod.POST,
          HttpEntity.EMPTY,
          String.class);

      assertEquals(HttpStatus.UNPROCESSABLE_CONTENT, response.getStatusCode());
    }

    @Test
    @DisplayName("retorna 422 si el subtotal no alcanza el minimo de 50 MXN (RN-VEN-12)")
    void iniciarCheckout_subtotalInsuficiente_retorna422() {
      // crearCarritoConProducto agrega cant 3 x $10.00 = $30.00 < $50.00 minimo
      UUID[] ids = crearCarritoConProducto();
      UUID carritoId = ids[0];

      ResponseEntity<String> response = restTemplate.exchange(
          BASE_URL + "/" + carritoId + "/checkout",
          HttpMethod.POST,
          HttpEntity.EMPTY,
          String.class);

      assertEquals(HttpStatus.UNPROCESSABLE_CONTENT, response.getStatusCode());
    }

    @Test
    @DisplayName("retorna 404 si el carrito no existe")
    void iniciarCheckout_carritoInexistente_retorna404() {
      ResponseEntity<String> response = restTemplate.exchange(
          BASE_URL + "/" + UUID.randomUUID() + "/checkout",
          HttpMethod.POST,
          HttpEntity.EMPTY,
          String.class);

      assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
  }

  @Nested
  @DisplayName("POST /api/v1/carritos/{carritoId}/checkout/completar")
  class CompletarCheckout {

    @Test
    @DisplayName("completa el checkout y retorna 200")
    void completarCheckout_retorna200() {
      UUID carritoId = crearCarritoEnCheckout();

      ResponseEntity<CarritoResponse> response = restTemplate.exchange(
          BASE_URL + "/" + carritoId + "/checkout/completar",
          HttpMethod.POST,
          HttpEntity.EMPTY,
          CarritoResponse.class);

      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("retorna 422 si el carrito no esta en checkout (RN-VEN-13)")
    void completarCheckout_carritoNoEnCheckout_retorna422() {
      // Carrito recien creado esta en ACTIVO, no EN_CHECKOUT
      ResponseEntity<CarritoResponse> carritoResponse = restTemplate.exchange(
          BASE_URL, HttpMethod.POST, jsonRequest(new CarritoRequest(UUID.randomUUID())),
          CarritoResponse.class);
      assertEquals(HttpStatus.CREATED, carritoResponse.getStatusCode());
      UUID carritoId = carritoResponse.getBody().carritoId();

      ResponseEntity<String> response = restTemplate.exchange(
          BASE_URL + "/" + carritoId + "/checkout/completar",
          HttpMethod.POST,
          HttpEntity.EMPTY,
          String.class);

      assertEquals(HttpStatus.UNPROCESSABLE_CONTENT, response.getStatusCode());
    }

    @Test
    @DisplayName("retorna 404 si el carrito no existe")
    void completarCheckout_carritoInexistente_retorna404() {
      ResponseEntity<String> response = restTemplate.exchange(
          BASE_URL + "/" + UUID.randomUUID() + "/checkout/completar",
          HttpMethod.POST,
          HttpEntity.EMPTY,
          String.class);

      assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
  }

  @Nested
  @DisplayName("POST /api/v1/carritos/{carritoId}/abandonar")
  class Abandonar {

    @Test
    @DisplayName("abandona el carrito en checkout y retorna 200")
    void abandonar_retorna200() {
      UUID carritoId = crearCarritoEnCheckout();

      ResponseEntity<CarritoResponse> response = restTemplate.exchange(
          BASE_URL + "/" + carritoId + "/abandonar",
          HttpMethod.POST,
          HttpEntity.EMPTY,
          CarritoResponse.class);

      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("retorna 422 si el carrito no esta en checkout (RN-VEN-14)")
    void abandonar_carritoNoEnCheckout_retorna422() {
      // Carrito recien creado esta en ACTIVO, no EN_CHECKOUT
      ResponseEntity<CarritoResponse> carritoResponse = restTemplate.exchange(
          BASE_URL, HttpMethod.POST, jsonRequest(new CarritoRequest(UUID.randomUUID())),
          CarritoResponse.class);
      assertEquals(HttpStatus.CREATED, carritoResponse.getStatusCode());
      UUID carritoId = carritoResponse.getBody().carritoId();

      ResponseEntity<String> response = restTemplate.exchange(
          BASE_URL + "/" + carritoId + "/abandonar",
          HttpMethod.POST,
          HttpEntity.EMPTY,
          String.class);

      assertEquals(HttpStatus.UNPROCESSABLE_CONTENT, response.getStatusCode());
    }

    @Test
    @DisplayName("retorna 404 si el carrito no existe")
    void abandonar_carritoInexistente_retorna404() {
      ResponseEntity<String> response = restTemplate.exchange(
          BASE_URL + "/" + UUID.randomUUID() + "/abandonar",
          HttpMethod.POST,
          HttpEntity.EMPTY,
          String.class);

      assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
  }
}
