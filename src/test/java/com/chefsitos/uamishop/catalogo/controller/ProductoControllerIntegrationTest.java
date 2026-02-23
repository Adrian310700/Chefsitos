package com.chefsitos.uamishop.catalogo.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.chefsitos.uamishop.catalogo.controller.dto.ProductoRequest;
import com.chefsitos.uamishop.catalogo.controller.dto.ProductoResponse;
import com.chefsitos.uamishop.catalogo.repository.ProductoJpaRepository;
import com.chefsitos.uamishop.catalogo.repository.CategoriaJpaRepository;
import com.chefsitos.uamishop.shared.domain.valueObject.ProductoId;

import java.math.BigDecimal;
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
}
