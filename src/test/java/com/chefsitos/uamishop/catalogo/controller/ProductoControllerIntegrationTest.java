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

  // HELPERS -------------------------
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

  private Producto crearProductoConImagenEnBD(String nombre, CategoriaId categoriaId) {
    Producto producto = Producto.crear(
        nombre,
        "Descripción " + nombre,
        new Money(new BigDecimal("32000.00"), "MXN"),
        categoriaId);

    producto.agregarImagen(
        Imagen.crear(
            "https://example.com/image.jpg",
            "Imagen principal",
            1));

    return productoRepository.save(producto);
  }

  private <T> HttpEntity<T> jsonRequest(T body) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    return new HttpEntity<>(body, headers);
  }

  // 1. CREAR PRODUCTO --------------------------------------
  @Nested
  @DisplayName("POST /api/v1/productos")
  class CreateProducto {

    // CASO DE EXITO
    @Test
    @DisplayName("crea producto y retorna 201 con Location header")
    void create_retorna201_yLocation() {

      // Crear categoría en BD
      Categoria categoria = crearCategoriaEnBD("Electronicos");

      // Construir request
      ProductoRequest body = new ProductoRequest(
          "MacBook Pro 16",
          "Laptop de alto rendimiento",
          new BigDecimal("45000.00"),
          "MXN",
          categoria.getCategoriaId().valor().toString());

      // Ejecutar POST
      ResponseEntity<ProductoResponse> response = restTemplate.exchange(
          BASE_URL,
          HttpMethod.POST,
          jsonRequest(body),
          ProductoResponse.class);

      // Validaciones
      assertEquals(HttpStatus.CREATED, response.getStatusCode());

      String location = response.getHeaders().getFirst("Location");
      assertNotNull(location);
      assertTrue(location.contains("/api/v1/productos/"));

      assertNotNull(response.getBody());
      assertNotNull(response.getBody().idProducto());
      assertEquals("MacBook Pro 16", response.getBody().nombreProducto());
      assertEquals(0, new BigDecimal("45000.00")
          .compareTo(response.getBody().precio()));
      assertEquals("MXN", response.getBody().moneda());

      // Validar persistencia en BD
      ProductoId productoId = ProductoId.of(response.getBody().idProducto().toString());

      assertTrue(productoRepository.findById(productoId).isPresent());
    }

    // CASOS DE FALLO
    // BAD REQUEST 400
    @Test
    @DisplayName("retorna 400 cuando nombreProducto es null")
    void create_retorna400_NombreEsNull() {

      Categoria categoria = crearCategoriaEnBD("Electronicos");

      ProductoRequest body = new ProductoRequest(
          null,
          "Descripción válida",
          new BigDecimal("100.00"),
          "MXN",
          categoria.getCategoriaId().valor().toString());

      ResponseEntity<String> response = restTemplate.exchange(
          BASE_URL,
          HttpMethod.POST,
          jsonRequest(body),
          String.class);

      assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("retorna 400 cuando precio es negativo")
    void create_retorna400_precioNegativo() {

      Categoria categoria = crearCategoriaEnBD("Electronicos");

      ProductoRequest body = new ProductoRequest(
          "Laptop",
          "Descripción válida",
          new BigDecimal("-100.00"),
          "MXN",
          categoria.getCategoriaId().valor().toString());

      ResponseEntity<String> response = restTemplate.exchange(
          BASE_URL,
          HttpMethod.POST,
          jsonRequest(body),
          String.class);

      assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("retorna 400 cuando moneda no tiene 3 caracteres")
    void create_retorna400_MonedaInvalida() {

      Categoria categoria = crearCategoriaEnBD("Electronicos");

      ProductoRequest body = new ProductoRequest(
          "Producto válido",
          "Descripción válida",
          new BigDecimal("100.00"),
          "MX", // inválido (solo 2 caracteres)
          categoria.getCategoriaId().valor().toString());

      ResponseEntity<String> response = restTemplate.exchange(
          BASE_URL,
          HttpMethod.POST,
          jsonRequest(body),
          String.class);

      assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("retorna 400 cuando idCategoria no es UUID válido")
    void create_retorna400_IdCategoriaFormatoInvalido() {

      ProductoRequest body = new ProductoRequest(
          "Producto válido",
          "Descripción válida",
          new BigDecimal("100.00"),
          "MXN",
          "123-no-es-uuid");

      ResponseEntity<String> response = restTemplate.exchange(
          BASE_URL,
          HttpMethod.POST,
          jsonRequest(body),
          String.class);

      assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("retorna 422 cuando el nombre tiene menos de 3 caracteres (regla de dominio)")
    void create_nombreMenorA3_retorna422() {

      // Crear categoría válida en BD
      Categoria categoria = crearCategoriaEnBD("Electronicos");

      // Construir request con nombre inválido (2 caracteres)
      ProductoRequest body = new ProductoRequest(
          "TV", // ❌ menor a 3 caracteres
          "Televisor 4K",
          new BigDecimal("10000.00"),
          "MXN",
          categoria.getCategoriaId().valor().toString());

      // Ejecutar POST
      ResponseEntity<String> response = restTemplate.exchange(
          BASE_URL,
          HttpMethod.POST,
          jsonRequest(body),
          String.class);

      // Validar status
      assertEquals(HttpStatus.UNPROCESSABLE_CONTENT, response.getStatusCode());

      // Validar que el mensaje corresponde a la regla de dominio
      assertNotNull(response.getBody());
      assertTrue(response.getBody().contains("El nombre debe tener entre 3 y 100 caracteres"));

      // Validar que NO se haya persistido nada
      assertEquals(0, productoRepository.count());
    }

    @Test
    @DisplayName("retorna 422 cuando el nombre tiene mas de 100 caracteres, pasa DTO pero falla dominio")
    void create_nombreMayorA100_retorna422() {

      // Crear categoría válida en BD
      Categoria categoria = crearCategoriaEnBD("Electronicos");

      // Generar nombre de 101 caracteres (pasa DTO: max 200)
      String nombre150 = "A".repeat(101);

      ProductoRequest body = new ProductoRequest(
          nombre150,
          "Laptop profesional",
          new BigDecimal("35000.00"),
          "MXN",
          categoria.getCategoriaId().valor().toString());

      // Ejecutar POST
      ResponseEntity<String> response = restTemplate.exchange(
          BASE_URL,
          HttpMethod.POST,
          jsonRequest(body),
          String.class);

      // Validar 422
      assertEquals(HttpStatus.UNPROCESSABLE_CONTENT, response.getStatusCode());

      // Validar mensaje de error del dominio
      assertNotNull(response.getBody());
      assertTrue(response.getBody()
          .contains("El nombre debe tener entre 3 y 100 caracteres"));

      // Validar que no se haya persistido nada
      assertEquals(0, productoRepository.count());
    }

    @Test
    @DisplayName("retorna 404 cuando la categoria no existe")
    void create_categoriaInexistente_retorna404() {

      // UUID válido pero no persistido en BD
      String categoriaInexistenteId = UUID.randomUUID().toString();

      ProductoRequest body = new ProductoRequest(
          "MacBook Pro",
          "Laptop profesional",
          new BigDecimal("45000.00"),
          "MXN",
          categoriaInexistenteId);

      // Ejecutar POST
      ResponseEntity<String> response = restTemplate.exchange(
          BASE_URL,
          HttpMethod.POST,
          jsonRequest(body),
          String.class);

      // Validar status 404
      assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

      // Validar mensaje
      assertNotNull(response.getBody());
      assertTrue(response.getBody()
          .contains("Categoria no encontrada con ID"));

      // Validar que no se haya persistido nada
      assertEquals(0, productoRepository.count());
    }
  }

  // 2. OBTENER PRODUCTO POR ID --------------------------------
  @Nested
  @DisplayName("GET /api/v1/productos/{id}")
  class GetProductoById {
    // TEST DE EXITO
    @Test
    @DisplayName("Crea un producto, lo busca en la BD por ID y retorna el mismo objeto")
    void getById_retorna200() {

      // Crear categoría
      Categoria categoria = crearCategoriaEnBD("Electronicos");

      // Crear producto
      Producto producto = crearProductoEnBD(
          "MacBook Pro",
          new BigDecimal("32000.00"),
          "MXN",
          categoria.getCategoriaId());

      UUID productoId = producto.getProductoId().valor();

      // Ejecutar GET
      ResponseEntity<ProductoResponse> response = restTemplate.getForEntity(
          BASE_URL + "/" + productoId,
          ProductoResponse.class);

      // Validaciones
      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertNotNull(response.getBody());

      ProductoResponse body = response.getBody();

      assertEquals(productoId, body.idProducto());
      assertEquals("MacBook Pro", body.nombreProducto());
      assertEquals("Descripción MacBook Pro", body.descripcion());
      assertEquals(0, new BigDecimal("32000.00").compareTo(body.precio()));
      assertEquals("MXN", body.moneda());
      assertEquals(categoria.getCategoriaId().valor(), body.idCategoria());
    }

    // TEST DE ERROR
    @Test
    @DisplayName("Retorna 404 cuando el producto no existe")
    void retorna404_siNoExiste() {

      // UUID válido pero que no existe en la BD
      UUID idInexistente = UUID.randomUUID();

      // Ejecutar GET
      ResponseEntity<String> response = restTemplate.getForEntity(
          BASE_URL + "/" + idInexistente,
          String.class);

      // Validar status
      assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("Retorna 400 cuando el ID no es un UUID válido")
    void retorna400_siUUIDInvalido() {

      // ID inválido (no cumple formato UUID)
      String idInvalido = "no-es-uuid";

      ResponseEntity<String> response = restTemplate.getForEntity(
          BASE_URL + "/" + idInvalido,
          String.class);

      // Validar status
      assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
  }

  // 3. OBTENER TODOS LOS PRODUCTOS ------------------------------
  @Nested
  @DisplayName("GET /api/v1/productos")
  class GetAll {
    // TEST DE EXITO
    @Test
    @DisplayName("retorna 200 y la lista de productos")
    void getAll_retorna200() {

      // Crear categoría usando helper
      Categoria categoria = crearCategoriaEnBD("Electrónica");

      // Crear productos usando helper
      crearProductoEnBD(
          "MacBook Pro",
          new BigDecimal("38000.00"),
          "MXN",
          categoria.getCategoriaId());

      crearProductoEnBD(
          "iPhone 15",
          new BigDecimal("18000.00"),
          "MXN",
          categoria.getCategoriaId());

      // Ejecutar GET
      ResponseEntity<ProductoResponse[]> response = restTemplate.getForEntity(
          BASE_URL,
          ProductoResponse[].class);

      // Validaciones
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
    // NO HAY TEST DE ERRORES QUE SE PUEDAN HACER
  }

  // 4. ACTIVAR PRODUCTO -------------------------------------------------------
  @Nested
  @DisplayName("POST /api/v1/productos/{id}/activar")
  class ActivarProducto {
    // TEST DE EXITO
    @Test
    @DisplayName("retorna 200 y activa el producto cuando existe")
    void activar_retorna200_yProductoActivado() {

      // Crear categoría usando helper
      Categoria categoria = crearCategoriaEnBD("Electrónica");

      // Crear producto con imagen usando helper
      Producto producto = crearProductoConImagenEnBD(
          "MacBook Pro",
          categoria.getCategoriaId());

      UUID productoId = producto.getProductoId().valor();

      // Ejecutar endpoint
      ResponseEntity<ProductoResponse> response = restTemplate.postForEntity(
          BASE_URL + "/" + productoId + "/activar",
          null,
          ProductoResponse.class);

      // Validaciones
      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertNotNull(response.getBody());

      ProductoResponse body = response.getBody();

      assertEquals(productoId, body.idProducto());
      assertTrue(body.disponible());

      // Validar persistencia en BD
      Producto productoActualizado = productoRepository
          .findById(ProductoId.of(productoId.toString()))
          .orElseThrow();

      assertTrue(productoActualizado.isDisponible());
    }
    // TEST DE ERRORES

    @Test
    @DisplayName("Retorna 404 y mensaje adecuado cuando el producto no existe")
    void retorna404_IdNoExiste() {

      UUID idInexistente = UUID.randomUUID();

      ResponseEntity<String> response = restTemplate.postForEntity(
          BASE_URL + "/" + idInexistente + "/activar",
          null,
          String.class);

      assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
      assertNotNull(response.getBody());
      assertTrue(response.getBody().contains("Producto no encontrado"));
    }

    @Test
    @DisplayName("Retorna 400 y mensaje adecuado cuando el ID no es UUID válido")
    void retorna400_UUIDInvalido() {

      String idInvalido = "abc";

      ResponseEntity<String> response = restTemplate.postForEntity(
          BASE_URL + "/" + idInvalido + "/activar",
          null,
          String.class);

      assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
      assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("Retorna 422 cuando el producto no tiene imágenes")
    void retorna422_ProductoSinImagenes() {

      // Crear categoría
      Categoria categoria = crearCategoriaEnBD("Electrónica");

      // Crear producto SIN imágenes
      Producto productoSinImagen = crearProductoEnBD(
          "Producto Sin Imagen",
          new BigDecimal("1000.00"),
          "MXN",
          categoria.getCategoriaId());

      // Asegurarse que la lista de imágenes esté vacía
      productoSinImagen.getImagenes().clear();
      productoRepository.save(productoSinImagen);

      UUID productoId = productoSinImagen.getProductoId().valor();

      // Ejecutar POST
      ResponseEntity<String> response = restTemplate.postForEntity(
          BASE_URL + "/" + productoId + "/activar",
          null,
          String.class);

      // Validar status
      assertEquals(HttpStatus.UNPROCESSABLE_CONTENT, response.getStatusCode());

      // Opcional: validar mensaje
      assertNotNull(response.getBody());
      assertTrue(response.getBody().contains("El producto solo puede volver a activarse si tiene al menos una imagen"));
    }
  }

  // 5. DESACTIVAR PRODUCTO ---------------------------
  @Nested
  @DisplayName("POST /api/v1/productos/{id}/desactivar")
  class DesactivarProducto {
    // TEST DE EXITO
    @Test
    @DisplayName("Crea producto, activa y lo desactiva posteriormente")
    void desactivarProducto_exito() {

      // Crear categoría
      Categoria categoria = crearCategoriaEnBD("Electrónica");

      // Crear producto con imagen
      Producto producto = crearProductoConImagenEnBD(
          "MacBook Pro",
          categoria.getCategoriaId());

      // Activarlo antes de desactivar
      producto.activar();
      productoRepository.save(producto);

      UUID productoId = producto.getProductoId().valor();

      // Ejecutar endpoint
      ResponseEntity<ProductoResponse> response = restTemplate.postForEntity(
          BASE_URL + "/" + productoId + "/desactivar",
          null,
          ProductoResponse.class);

      // Validaciones
      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertNotNull(response.getBody());
      assertFalse(response.getBody().disponible());
    }

    // TEST DE ERRORES

    @Test
    @DisplayName("Retorna 404 cuando el producto no existe al intentar desactivarlo")
    void retorna404_ProductoNoExiste() {

      // UUID válido pero que no existe en la base de datos
      UUID idInexistente = UUID.randomUUID();

      // Ejecutar POST al endpoint de desactivación
      ResponseEntity<String> response = restTemplate.postForEntity(
          BASE_URL + "/" + idInexistente + "/desactivar",
          null,
          String.class);

      // Validar que devuelve 404
      assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("Retorna 400 cuando el ID no es un UUID válido al desactivar producto")
    void retorna400_UUIDInvalido() {

      // ID inválido (no cumple formato UUID)
      String idInvalido = "no-es-uuid";

      // Ejecutar POST al endpoint de desactivación
      ResponseEntity<String> response = restTemplate.postForEntity(
          BASE_URL + "/" + idInvalido + "/desactivar",
          null,
          String.class);

      // Validar que devuelve 400
      assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("Retorna 422 cuando el producto ya está desactivado")
    void retorna422_siYaDesactivado() {

      // Crear categoría
      Categoria categoria = crearCategoriaEnBD("Electrónica");

      // Crear producto con imagen
      Producto producto = crearProductoConImagenEnBD(
          "Producto Ya Desactivado",
          categoria.getCategoriaId());

      productoRepository.save(producto);

      UUID productoId = producto.getProductoId().valor();

      // Ejecutar POST al endpoint de desactivación
      ResponseEntity<String> response = restTemplate.postForEntity(
          BASE_URL + "/" + productoId + "/desactivar",
          null,
          String.class);

      // Validar status 422
      assertEquals(HttpStatus.UNPROCESSABLE_CONTENT, response.getStatusCode());

      // Validar mensaje de regla de negocio
      assertNotNull(response.getBody());
      assertTrue(response.getBody().contains("No se puede volver a desactivar un producto ya desactivado"));
    }
  }

  // 6. ACTUALIZAR PRODUCTO -------------------------------------
  @Nested
  @DisplayName("PATCH /api/v1/productos/{id}")
  class ActualizarProducto {
    // TEST DE EXITO
    @Test
    @DisplayName("Actualiza producto con PATCH retorna 200")
    void actualizarProducto_exito() {

      // Crear categoría original
      Categoria categoria = crearCategoriaEnBD("Electrónica");

      // Crear nueva categoría
      Categoria nuevaCategoria = crearCategoriaEnBD("Computación");

      // Crear producto base usando helper
      Producto producto = crearProductoEnBD(
          "MacBook Pro",
          new BigDecimal("32000.00"),
          "MXN",
          categoria.getCategoriaId());

      // Construir request
      ProductoPatchRequest request = new ProductoPatchRequest(
          "MacBook Pro M3",
          "Laptop profesional actualizada",
          new BigDecimal("35000.00"),
          "USD",
          nuevaCategoria.getCategoriaId().valor().toString());

      // Ejecutar PATCH
      ResponseEntity<ProductoResponse> response = restTemplate.exchange(
          BASE_URL + "/" + producto.getProductoId().valor(),
          HttpMethod.PATCH,
          jsonRequest(request),
          ProductoResponse.class);

      // Validaciones
      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertNotNull(response.getBody());

      ProductoResponse body = response.getBody();

      assertEquals("MacBook Pro M3", body.nombreProducto());
      assertEquals("Laptop profesional actualizada", body.descripcion());
      assertEquals("USD", body.moneda());
      assertEquals("35000.00", body.precio().toString());
    }

    // TEST DE ERRORES
    @Test
    @DisplayName("Retorna 404 si el producto no existe")
    void retorna404_ProductoInexistente() {
      UUID idInexistente = UUID.randomUUID();

      ProductoPatchRequest request = new ProductoPatchRequest(
          "Nombre",
          "Descripción",
          new BigDecimal("1000"),
          "USD",
          null);

      ResponseEntity<String> response = restTemplate.exchange(
          BASE_URL + "/" + idInexistente,
          HttpMethod.PATCH,
          jsonRequest(request),
          String.class);

      assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
  }

  @Test
  @DisplayName("Retorna 400 si el ID del producto no es UUID válido")
  void retorna400_UUIDInvalido() {
    String idInvalido = "no-es-uuid";

    ProductoPatchRequest request = new ProductoPatchRequest(
        "Nombre",
        "Descripción",
        new BigDecimal("1000"),
        "USD",
        null);

    ResponseEntity<String> response = restTemplate.exchange(
        BASE_URL + "/" + idInvalido,
        HttpMethod.PATCH,
        jsonRequest(request),
        String.class);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  @DisplayName("Retorna 400 si el nombre es inválido (<3 o >100 caracteres)")
  void retorna400_NombreInvalido() {
    Categoria categoria = crearCategoriaEnBD("Electrónica");
    Producto producto = crearProductoEnBD("MacBook Pro", new BigDecimal("32000"), "USD", categoria.getCategoriaId());

    ProductoPatchRequest request = new ProductoPatchRequest(
        "AB", // nombre demasiado corto
        "Descripción válida",
        null,
        null,
        null);

    ResponseEntity<String> response = restTemplate.exchange(
        BASE_URL + "/" + producto.getProductoId().valor(),
        HttpMethod.PATCH,
        jsonRequest(request),
        String.class);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertTrue(response.getBody().contains("El nombre debe tener entre 3 y 100 caracteres"));
  }

  @Test
  @DisplayName("Retorna 400 si la descripción excede 500 caracteres")
  void retorna400_DescripcionInvalida() {
    Categoria categoria = crearCategoriaEnBD("Electrónica");
    Producto producto = crearProductoEnBD("MacBook Pro", new BigDecimal("32000"), "USD", categoria.getCategoriaId());

    String descripcionLarga = "A".repeat(800);

    ProductoPatchRequest request = new ProductoPatchRequest(
        "Nombre válido",
        descripcionLarga,
        null,
        null,
        null);

    ResponseEntity<String> response = restTemplate.exchange(
        BASE_URL + "/" + producto.getProductoId().valor(),
        HttpMethod.PATCH,
        jsonRequest(request),
        String.class);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertTrue(response.getBody().contains("La descripción no debe exceder los 500 caracteres"));
  }

  @Test
  @DisplayName("Retorna 400 si el precio es negativo o cero")
  void retorna400_PrecioInvalido() {
    Categoria categoria = crearCategoriaEnBD("Electrónica");
    Producto producto = crearProductoEnBD("MacBook Pro", new BigDecimal("32000"), "USD", categoria.getCategoriaId());

    ProductoPatchRequest request = new ProductoPatchRequest(
        "Nombre válido",
        "Descripción válida",
        new BigDecimal("-100"), // precio inválido
        "USD",
        null);

    ResponseEntity<String> response = restTemplate.exchange(
        BASE_URL + "/" + producto.getProductoId().valor(),
        HttpMethod.PATCH,
        jsonRequest(request),
        String.class);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertTrue(response.getBody().contains("El precio debe ser positivo"));
  }

  @Test
  @DisplayName("Retorna 400 si la moneda no tiene 3 caracteres")
  void retorna400_MonedaInvalida() {
    Categoria categoria = crearCategoriaEnBD("Electrónica");
    Producto producto = crearProductoEnBD("MacBook Pro", new BigDecimal("32000"), "USD", categoria.getCategoriaId());

    ProductoPatchRequest request = new ProductoPatchRequest(
        "Nombre válido",
        "Descripción válida",
        null,
        "US", // inválido
        null);

    ResponseEntity<String> response = restTemplate.exchange(
        BASE_URL + "/" + producto.getProductoId().valor(),
        HttpMethod.PATCH,
        jsonRequest(request),
        String.class);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertTrue(response.getBody().contains("La moneda debe tener exactamente 3 caracteres"));
  }

  @Test
  @DisplayName("Retorna 400 si el ID de categoría no es un UUID válido")
  void retorna400_IdCategoriaInvalido() {
    Categoria categoria = crearCategoriaEnBD("Electrónica");
    Producto producto = crearProductoEnBD("MacBook Pro", new BigDecimal("32000"), "USD", categoria.getCategoriaId());

    ProductoPatchRequest request = new ProductoPatchRequest(
        "Nombre válido",
        "Descripción válida",
        null,
        null,
        "no-es-uuid" // inválido
    );

    ResponseEntity<String> response = restTemplate.exchange(
        BASE_URL + "/" + producto.getProductoId().valor(),
        HttpMethod.PATCH,
        jsonRequest(request),
        String.class);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  @DisplayName("Retorna 404 si la categoría indicada no existe")
  void retorna404_CategoriaInexistente() {
    Categoria categoria = crearCategoriaEnBD("Electrónica");
    Producto producto = crearProductoEnBD("MacBook Pro", new BigDecimal("32000"), "USD", categoria.getCategoriaId());

    UUID categoriaInexistente = UUID.randomUUID();

    ProductoPatchRequest request = new ProductoPatchRequest(
        "Nombre válido",
        "Descripción válida",
        null,
        null,
        categoriaInexistente.toString());

    ResponseEntity<String> response = restTemplate.exchange(
        BASE_URL + "/" + producto.getProductoId().valor(),
        HttpMethod.PATCH,
        jsonRequest(request),
        String.class);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    assertTrue(response.getBody().contains("Categoria no encontrada"));
  }
}
