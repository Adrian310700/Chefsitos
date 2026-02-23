package com.chefsitos.uamishop.catalogo.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

import com.chefsitos.uamishop.catalogo.controller.dto.CategoriaRequest;
import com.chefsitos.uamishop.catalogo.controller.dto.CategoriaResponse;
import com.chefsitos.uamishop.catalogo.domain.entity.Categoria;
import com.chefsitos.uamishop.catalogo.domain.valueObject.CategoriaId;
import com.chefsitos.uamishop.catalogo.repository.CategoriaJpaRepository;
import com.chefsitos.uamishop.catalogo.repository.ProductoJpaRepository;
import org.junit.jupiter.api.Nested;

import java.util.UUID;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
public class CategoriaControllerIntegrationTest {

  private static final String BASE_URL = "/api/v1/categorias";

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

  // Funcion para crear un request body de categoria cada que sea necesario
  private CategoriaRequest crearRequestCategoria(String nombre, String descripcion, UUID idPadre) {
    CategoriaRequest request = new CategoriaRequest(
        nombre,
        descripcion,
        idPadre);

    return request;
  }

  // Funcion para crear un objeto de tipo categoria
  private Categoria crearCategoria(String nombre, String descripcion, CategoriaId idPadre) {
    Categoria categoria = Categoria.crear(
        idPadre,
        nombre,
        descripcion);

    return categoria;
  }

  // 1. CREAR CATEGORIA ------------------------
  @Nested
  @DisplayName("POST /api/v1/categorias")
  class CrearCategoria {
    // TEST DE EXITO
    @Test
    @DisplayName("Categoria sin asignar padre, retorna 201 y response")
    void crearCategoria_exito_sinPadre() {

      CategoriaRequest request = crearRequestCategoria("Electrónica", "Dispositivos electrónicos", null);
      // Indicar el tipo JSON
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);

      HttpEntity<CategoriaRequest> entity = new HttpEntity<>(request, headers);
      // Hacer la operacion POST
      ResponseEntity<CategoriaResponse> response = restTemplate.postForEntity(
          BASE_URL,
          entity,
          CategoriaResponse.class);

      // Verificar que la respuesta sea la esperada
      assertEquals(HttpStatus.CREATED, response.getStatusCode());

      // Obtener location
      assertNotNull(response.getHeaders().getLocation());

      // Comprobar que la respuesta sea la misma que el request
      assertNotNull(response.getBody());
      assertEquals("Electrónica", response.getBody().nombreCategoria());
      assertEquals("Dispositivos electrónicos", response.getBody().descripcion());
      assertNull(response.getBody().idCategoriaPadre());
    }

    // TEST DE EXITO
    @Test
    @DisplayName("Categoria asignando un padre, retorna 201 y response")
    void crearCategoria_exito_conPadre() {

      // Categoria padre
      Categoria categoriaPadre = crearCategoria("Tecnologia", "Categoria principal", CategoriaId.generar());
      categoriaRepository.save(categoriaPadre);

      CategoriaRequest request = crearRequestCategoria(
          "Computadoras",
          "Subcategoria",
          UUID.fromString(categoriaPadre.getCategoriaId().valor().toString()));

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);

      HttpEntity<CategoriaRequest> entity = new HttpEntity<>(request, headers);

      ResponseEntity<CategoriaResponse> response = restTemplate.postForEntity(
          BASE_URL,
          entity,
          CategoriaResponse.class);

      assertEquals(HttpStatus.CREATED, response.getStatusCode());
      assertNotNull(response.getBody());
      assertEquals(categoriaPadre.getCategoriaId().valor().toString(),
          response.getBody().idCategoriaPadre().toString());
    }

    // TEST DE ERRORES
    @Test
    @DisplayName("Debe retornar 400 cuando el nombre es null")
    void crearCategoria_NombreNull() {

      CategoriaRequest request = new CategoriaRequest(
          null,
          "Descripción válida",
          null);

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);

      HttpEntity<CategoriaRequest> entity = new HttpEntity<>(request, headers);

      ResponseEntity<String> response = restTemplate.postForEntity(
          BASE_URL,
          entity,
          String.class);

      assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("Debe retornar 400 cuando el nombre es vacío")
    void crearCategoria_NombreVacio() {

      CategoriaRequest request = new CategoriaRequest(
          "",
          "Descripción válida",
          null);

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);

      HttpEntity<CategoriaRequest> entity = new HttpEntity<>(request, headers);

      ResponseEntity<String> response = restTemplate.postForEntity(
          BASE_URL,
          entity,
          String.class);

      assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("Debe retornar 400 cuando el nombre está en blanco")
    void crearCategoria_NombreEnBlanco() {

      CategoriaRequest request = new CategoriaRequest(
          "   ",
          "Descripción válida",
          null);

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);

      HttpEntity<CategoriaRequest> entity = new HttpEntity<>(request, headers);

      ResponseEntity<String> response = restTemplate.postForEntity(
          BASE_URL,
          entity,
          String.class);

      assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("Debe retornar 400 cuando el nombre supera los 200 caracteres")
    void crearCategoria_NombreInvalido() {

      String nombreLargo = "a".repeat(201);

      CategoriaRequest request = new CategoriaRequest(
          nombreLargo,
          "Descripción válida",
          null);

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);

      HttpEntity<CategoriaRequest> entity = new HttpEntity<>(request, headers);

      ResponseEntity<String> response = restTemplate.postForEntity(
          BASE_URL,
          entity,
          String.class);

      assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("Debe retornar 422 cuando el nombre tiene menos de 3 caracteres")
    void crearCategoria_NombreInvalido_2Caracteres() {

      CategoriaRequest request = new CategoriaRequest(
          "ab",
          "Descripción válida",
          null);

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);

      HttpEntity<CategoriaRequest> entity = new HttpEntity<>(request, headers);

      ResponseEntity<String> response = restTemplate.postForEntity(
          BASE_URL,
          entity,
          String.class);

      assertEquals(HttpStatus.UNPROCESSABLE_CONTENT, response.getStatusCode());
    }

    @Test
    @DisplayName("Debe retornar 422 cuando el nombre supera los 100 caracteres")
    void crearCategoria_NombreInvalido_MayorA100() {

      String nombreLargo = "a".repeat(101); // pasa DTO (max 200), falla dominio (max 100)

      CategoriaRequest request = new CategoriaRequest(
          nombreLargo,
          "Descripción válida",
          null);

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);

      HttpEntity<CategoriaRequest> entity = new HttpEntity<>(request, headers);

      ResponseEntity<String> response = restTemplate.postForEntity(
          BASE_URL,
          entity,
          String.class);

      assertEquals(HttpStatus.UNPROCESSABLE_CONTENT, response.getStatusCode());
    }

    @Test
    @DisplayName("Debe retornar 422 cuando la descripción supera los 500 caracteres")
    void crearCategoria_DescripcionInvalido() {

      String descripcionLarga = "a".repeat(501); // pasa DTO (max 1000), falla dominio (max 500)

      CategoriaRequest request = new CategoriaRequest(
          "Nombre válido",
          descripcionLarga,
          null);

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);

      HttpEntity<CategoriaRequest> entity = new HttpEntity<>(request, headers);

      ResponseEntity<String> response = restTemplate.postForEntity(
          BASE_URL,
          entity,
          String.class);

      assertEquals(HttpStatus.UNPROCESSABLE_CONTENT, response.getStatusCode());
    }

    @Test
    @DisplayName("Debe retornar 404 cuando la categoría padre no existe")
    void crearCategoria_PadreInexsistente() {

      UUID idInexistente = UUID.randomUUID();

      CategoriaRequest request = new CategoriaRequest(
          "Categoría válida",
          "Descripción válida",
          idInexistente);

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);

      HttpEntity<CategoriaRequest> entity = new HttpEntity<>(request, headers);

      ResponseEntity<String> response = restTemplate.postForEntity(
          BASE_URL,
          entity,
          String.class);

      assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
  }

  // 2. BUSCAR CATEGORIA POR ID -----------------------------
  @Nested
  @DisplayName("GET /api/v1/categorias/{id}")
  class BuscarCategoriaPorId {
    // TEST DE EXITO
    @Test
    @DisplayName("Retorna 200 y response")
    void buscarCategoriaPorId_exito() {

      // Crear una categoria
      Categoria categoria = crearCategoria("Electrónica", "Dispositivos electrónicos", CategoriaId.generar());

      categoriaRepository.save(categoria);

      ResponseEntity<CategoriaResponse> response = restTemplate.getForEntity(
          BASE_URL + "/" + categoria.getCategoriaId().valor(),
          CategoriaResponse.class);

      // Validar respuesta
      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertNotNull(response.getBody());

      assertEquals(categoria.getCategoriaId().valor(),
          response.getBody().idCategoria());
      assertEquals("Electrónica",
          response.getBody().nombreCategoria());
      assertEquals("Dispositivos electrónicos",
          response.getBody().descripcion());
      assertNull(response.getBody().idCategoriaPadre());
    }

    // TEST DE ERRORES
    @Test
    @DisplayName("Debe retornar 404 cuando la categoría no existe")
    void buscarCategoriaPorId_CategoriaNoExiste() {

      UUID idInexistente = UUID.randomUUID();

      ResponseEntity<String> response = restTemplate.getForEntity(
          BASE_URL + "/" + idInexistente,
          String.class);

      assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
      assertTrue(response.getBody().contains("Categoria no encontrada"));
    }

    @Test
    @DisplayName("Debe retornar 400 cuando el UUID tiene formato inválido")
    void buscarCategoriaPorId_error_uuidInvalido() {

      String uuidInvalido = "abc123-no-es-uuid";

      ResponseEntity<String> response = restTemplate.getForEntity(
          BASE_URL + "/" + uuidInvalido,
          String.class);

      assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
      assertTrue(response.getBody().contains("Failed to convert"));
    }

  }

  // 3. BUSCAR TODAS LAS CATEGORIAS ----------
  @Nested
  @DisplayName("GET /api/v1/categorias")
  class BuscarTodasCategorias {
    // TEST DE EXITO
    @Test
    @DisplayName("Retorna 200 y List de response")
    void buscarTodasCategorias_exito() {

      // Crear 2 categorías
      Categoria categoria1 = crearCategoria("Electrónica", "Dispositivos electrónicos", CategoriaId.generar());
      Categoria categoria2 = crearCategoria("Ropa", "Prendas de vestir", CategoriaId.generar());

      categoriaRepository.save(categoria1);
      categoriaRepository.save(categoria2);

      // Ejecutar GET usando ParameterizedTypeReference
      ResponseEntity<java.util.List<CategoriaResponse>> response = restTemplate.exchange(
          BASE_URL,
          HttpMethod.GET,
          null,
          new ParameterizedTypeReference<java.util.List<CategoriaResponse>>() {
          });

      // Validar respuesta
      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertNotNull(response.getBody());
      assertEquals(2, response.getBody().size());

      java.util.List<UUID> ids = response.getBody().stream()
          .map(CategoriaResponse::idCategoria)
          .toList();

      assertTrue(ids.contains(categoria1.getCategoriaId().valor()));
      assertTrue(ids.contains(categoria2.getCategoriaId().valor()));
    }
    // NO HAY TEST DE ERRORES QUE SE PUEDAN HACER
  }

  // 4. ACTUALIZAR CATEGORIA --------------
  @Nested
  @DisplayName("PUT /api/v1/categorias/{id}")
  class ActualizarCategoria {
    // TEST DE EXITO
    @Test
    @DisplayName("Retorna 200 y response de categoria actualizado")
    void actualizarCategoria_exito() {

      // Crear categoría padre
      Categoria padre = crearCategoria("Tecnología", "Categoría principal", CategoriaId.generar());
      categoriaRepository.save(padre);

      // Crear categoría a actualizar
      Categoria categoria = crearCategoria("Electrónica", "Dispositivos electrónicos", CategoriaId.generar());
      categoriaRepository.save(categoria);

      // Request
      CategoriaRequest request = crearRequestCategoria(
          "Electrónica Actualizada",
          "Nueva descripción válida",
          UUID.fromString(padre.getCategoriaId().valor().toString()));

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);

      HttpEntity<CategoriaRequest> entity = new HttpEntity<>(request, headers);

      ResponseEntity<CategoriaResponse> response = restTemplate.exchange(
          BASE_URL + "/" + categoria.getCategoriaId().valor().toString(),
          HttpMethod.PUT,
          entity,
          CategoriaResponse.class);

      // Validaciones
      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertNotNull(response.getBody());

      assertEquals("Electrónica Actualizada",
          response.getBody().nombreCategoria());
      assertEquals("Nueva descripción válida",
          response.getBody().descripcion());

      assertEquals(padre.getCategoriaId().valor(),
          response.getBody().idCategoriaPadre());
    }

    // TEST DE ERRORES
    @Test
    @DisplayName("Debe retornar 400 cuando el nombre es null")
    void actualizarCategoria_NombreNull() {

      UUID id = UUID.randomUUID();

      CategoriaRequest request = new CategoriaRequest(
          null,
          "Descripción válida",
          null);

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);

      HttpEntity<CategoriaRequest> entity = new HttpEntity<>(request, headers);

      ResponseEntity<String> response = restTemplate.exchange(
          BASE_URL + "/" + id,
          HttpMethod.PUT,
          entity,
          String.class);

      assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("Debe retornar 400 cuando el nombre supera los 200 caracteres")
    void actualizarCategoria_NombreMayorA200() {

      UUID id = UUID.randomUUID();

      String nombreLargo = "a".repeat(201);

      CategoriaRequest request = new CategoriaRequest(
          nombreLargo,
          "Descripción válida",
          null);

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);

      HttpEntity<CategoriaRequest> entity = new HttpEntity<>(request, headers);

      ResponseEntity<String> response = restTemplate.exchange(
          BASE_URL + "/" + id,
          HttpMethod.PUT,
          entity,
          String.class);

      assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("Debe retornar 400 cuando la descripción supera los 1000 caracteres")
    void actualizarCategoria_DescripcionMayorA1000() {

      UUID id = UUID.randomUUID();

      String descripcionLarga = "a".repeat(1001);

      CategoriaRequest request = new CategoriaRequest(
          "Nombre válido",
          descripcionLarga,
          null);

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);

      HttpEntity<CategoriaRequest> entity = new HttpEntity<>(request, headers);

      ResponseEntity<String> response = restTemplate.exchange(
          BASE_URL + "/" + id,
          HttpMethod.PUT,
          entity,
          String.class);

      assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("Debe retornar 422 cuando el nombre tiene menos de 3 caracteres")
    void actualizarCategoria_NombreMenorA3() {

      // Crear categoría existente
      Categoria categoria = crearCategoria("Electrónica", "Descripción válida", CategoriaId.generar());
      categoriaRepository.save(categoria);

      CategoriaRequest request = new CategoriaRequest(
          "ab", // pasa DTO, falla dominio (<3)
          "Descripción válida",
          null);

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);

      HttpEntity<CategoriaRequest> entity = new HttpEntity<>(request, headers);

      ResponseEntity<String> response = restTemplate.exchange(
          BASE_URL + "/" + categoria.getCategoriaId().valor(),
          HttpMethod.PUT,
          entity,
          String.class);

      assertEquals(HttpStatus.UNPROCESSABLE_CONTENT, response.getStatusCode());
    }

    @Test
    @DisplayName("Debe retornar 422 cuando el nombre supera los 100 caracteres")
    void actualizarCategoria_NombreMayorA100() {

      Categoria categoria = crearCategoria("Electrónica", "Descripción válida", CategoriaId.generar());
      categoriaRepository.save(categoria);

      String nombreLargo = "a".repeat(101); // pasa DTO (≤200), falla dominio (>100)

      CategoriaRequest request = new CategoriaRequest(
          nombreLargo,
          "Descripción válida",
          null);

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);

      HttpEntity<CategoriaRequest> entity = new HttpEntity<>(request, headers);

      ResponseEntity<String> response = restTemplate.exchange(
          BASE_URL + "/" + categoria.getCategoriaId().valor(),
          HttpMethod.PUT,
          entity,
          String.class);

      assertEquals(HttpStatus.UNPROCESSABLE_CONTENT, response.getStatusCode());
    }

    @Test
    @DisplayName("Debe retornar 422 cuando la descripción supera los 500 caracteres")
    void actualizarCategoria_DescripcionMayorA500() {

      Categoria categoria = crearCategoria("Electrónica", "Descripción válida", CategoriaId.generar());
      categoriaRepository.save(categoria);

      String descripcionLarga = "a".repeat(501); // pasa DTO (≤1000), falla dominio (>500)

      CategoriaRequest request = new CategoriaRequest(
          "Nombre válido",
          descripcionLarga,
          null);

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);

      HttpEntity<CategoriaRequest> entity = new HttpEntity<>(request, headers);

      ResponseEntity<String> response = restTemplate.exchange(
          BASE_URL + "/" + categoria.getCategoriaId().valor(),
          HttpMethod.PUT,
          entity,
          String.class);

      assertEquals(HttpStatus.UNPROCESSABLE_CONTENT, response.getStatusCode());
    }

    @Test
    @DisplayName("Debe retornar 400 cuando el UUID es inválido")
    void actualizarCategoria_UUIDInvalido() {

      String uuidInvalido = "no-es-un-uuid";

      CategoriaRequest request = new CategoriaRequest(
          "Nombre válido",
          "Descripción válida",
          null);

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);

      HttpEntity<CategoriaRequest> entity = new HttpEntity<>(request, headers);

      ResponseEntity<String> response = restTemplate.exchange(
          BASE_URL + "/" + uuidInvalido,
          HttpMethod.PUT,
          entity,
          String.class);

      assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("Debe retornar 404 cuando la categoría no existe")
    void actualizarCategoria_CategoriaInexistente() {

      UUID idInexistente = UUID.randomUUID();

      CategoriaRequest request = new CategoriaRequest(
          "Nombre válido",
          "Descripción válida",
          null);

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);

      HttpEntity<CategoriaRequest> entity = new HttpEntity<>(request, headers);

      ResponseEntity<String> response = restTemplate.exchange(
          BASE_URL + "/" + idInexistente,
          HttpMethod.PUT,
          entity,
          String.class);

      assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
  }
}
