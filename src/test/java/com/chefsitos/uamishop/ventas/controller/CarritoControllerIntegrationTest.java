package com.chefsitos.uamishop.ventas.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
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

import com.chefsitos.uamishop.ventas.controller.dto.CarritoRequest;
import com.chefsitos.uamishop.ventas.controller.dto.CarritoResponse;
import com.chefsitos.uamishop.ventas.domain.valueObject.CarritoId;
import com.chefsitos.uamishop.ventas.repository.CarritoJpaRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class CarritoControllerIntegrationTest {

  private static final String BASE_URL = "/api/v1/carrito";

  @Autowired
  private TestRestTemplate restTemplate;

  @Autowired
  private CarritoJpaRepository carritoRepository;

  @AfterEach
  void cleanUp() {
    carritoRepository.deleteAll();
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
      assertTrue(response.getHeaders().getFirst("Location").contains("/api/v1/carrito/"));

      assertNotNull(response.getBody());
      assertNotNull(response.getBody().carritoId());

      CarritoId carritoId = CarritoId.of(response.getBody().carritoId().toString());
      assertTrue(carritoRepository.findById(carritoId).isPresent());
    }
  }

}
