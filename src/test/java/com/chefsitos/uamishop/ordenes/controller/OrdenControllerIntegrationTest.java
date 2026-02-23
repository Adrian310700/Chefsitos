package com.chefsitos.uamishop.ordenes.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
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

import com.chefsitos.uamishop.catalogo.controller.dto.ProductoRequest;
import com.chefsitos.uamishop.catalogo.controller.dto.ProductoResponse;
import com.chefsitos.uamishop.catalogo.domain.entity.Categoria;
import com.chefsitos.uamishop.catalogo.domain.valueObject.CategoriaId;
import com.chefsitos.uamishop.catalogo.repository.CategoriaJpaRepository;
import com.chefsitos.uamishop.catalogo.repository.ProductoJpaRepository;
import com.chefsitos.uamishop.ordenes.controller.dto.CancelarOrdenRequest;
import com.chefsitos.uamishop.ordenes.controller.dto.DireccionEnvioRequest;
import com.chefsitos.uamishop.ordenes.controller.dto.InfoEnvioRequest;
import com.chefsitos.uamishop.ordenes.controller.dto.OrdenRequest;
import com.chefsitos.uamishop.ordenes.controller.dto.OrdenRequest.ItemOrdenRequest;
import com.chefsitos.uamishop.ordenes.controller.dto.OrdenResponseDTO;
import com.chefsitos.uamishop.ordenes.controller.dto.PagarOrdenRequest;
import com.chefsitos.uamishop.ordenes.domain.enumeration.EstadoOrden;
import com.chefsitos.uamishop.ordenes.repository.OrdenJpaRepository;
import com.chefsitos.uamishop.shared.ApiError;

/**
 * Pruebas de integración para el controlador de Ordenes.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class OrdenControllerIntegrationTest {

  private static final String BASE_URL = "/api/v1/ordenes";
  private static final int HTTP_422 = 422;

  @Autowired
  private TestRestTemplate restTemplate;

  @Autowired
  private ProductoJpaRepository productoRepository;

  @Autowired
  private CategoriaJpaRepository categoriaRepository;

  @Autowired
  private OrdenJpaRepository ordenRepository;

  @AfterEach
  void cleanUp() {
    ordenRepository.deleteAll();
    productoRepository.deleteAll();
    categoriaRepository.deleteAll();
  }

  private UUID givenProductoEnCatalogo() {
    UUID categoriaId = UUID.randomUUID();
    Categoria categoria = Categoria.crear(new CategoriaId(categoriaId), "Electrónicos", "Dispositivos electrónicos");
    categoriaRepository.save(categoria);

    ProductoRequest request = new ProductoRequest(
        "Audífonos",
        "Audífonos inalámbricos",
        new BigDecimal("1200"),
        "MXN",
        categoriaId.toString());

    ResponseEntity<ProductoResponse> response = restTemplate.postForEntity(
        "/api/v1/productos",
        request,
        ProductoResponse.class);

    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertNotNull(response.getBody());
    return response.getBody().idProducto();
  }

  private OrdenRequest buildOrdenRequest(UUID clienteId, UUID productoId) {
    DireccionEnvioRequest direccion = new DireccionEnvioRequest(
        "Juan Pérez",
        "Av. Universidad 3000",
        "Ciudad de México",
        "CDMX",
        "04510",
        "5512345678",
        "Dejar en portería");

    List<ItemOrdenRequest> items = List.of(
        new ItemOrdenRequest(productoId.toString(), new BigDecimal("2")));

    return new OrdenRequest(clienteId, direccion, items);
  }

  private UUID givenOrdenCreada() {
    UUID productoId = givenProductoEnCatalogo();
    UUID clienteId = UUID.randomUUID();
    ResponseEntity<OrdenResponseDTO> createResp = restTemplate.postForEntity(
        BASE_URL,
        buildOrdenRequest(clienteId, productoId),
        OrdenResponseDTO.class);
    assertEquals(HttpStatus.CREATED, createResp.getStatusCode());
    assertNotNull(createResp.getBody());
    return createResp.getBody().id();
  }

  @Nested
  @DisplayName("POST /api/v1/ordenes")
  class CrearOrden {

    @Test
    @DisplayName("crea una orden y devuelve 201 con Location y estado PENDIENTE")
    void crearOrden_retorna201ConLocationYEstadoPendiente() {
      UUID productoId = givenProductoEnCatalogo();
      UUID clienteId = UUID.randomUUID();

      OrdenRequest body = buildOrdenRequest(clienteId, productoId);
      HttpEntity<OrdenRequest> request = new HttpEntity<>(body);

      ResponseEntity<OrdenResponseDTO> response = restTemplate.exchange(
          BASE_URL,
          HttpMethod.POST,
          request,
          OrdenResponseDTO.class);

      assertEquals(HttpStatus.CREATED, response.getStatusCode());

      String location = response.getHeaders().getFirst("Location");
      assertNotNull(location);
      assertTrue(location.contains("/api/v1/ordenes/"));

      OrdenResponseDTO bodyResponse = response.getBody();
      assertNotNull(bodyResponse);
      assertNotNull(bodyResponse.id());
      assertEquals(clienteId, bodyResponse.clienteId());
      assertEquals(EstadoOrden.PENDIENTE, bodyResponse.estado());
    }
  }

  @Nested
  @DisplayName("GET /api/v1/ordenes")
  class ConsultarOrdenes {

    @Test
    @DisplayName("devuelve la lista de órdenes con 200")
    void listarOrdenes_retorna200YLista() {
      UUID productoId = givenProductoEnCatalogo();
      UUID clienteId = UUID.randomUUID();
      restTemplate.postForEntity(BASE_URL, buildOrdenRequest(clienteId, productoId), OrdenResponseDTO.class);

      ResponseEntity<OrdenResponseDTO[]> response = restTemplate.getForEntity(
          BASE_URL,
          OrdenResponseDTO[].class);

      assertEquals(HttpStatus.OK, response.getStatusCode());
      OrdenResponseDTO[] ordenes = response.getBody();
      assertNotNull(ordenes);
      assertTrue(ordenes.length >= 1);
    }
  }

  @Nested
  @DisplayName("Flujo completo de una orden")
  class FlujoCompleto {

    @Test
    @DisplayName("flujo de confirmación, pago, preparación, envío y entrega")
    void flujoCompleto_cambiaEstadosEnOrden() {
      UUID productoId = givenProductoEnCatalogo();
      UUID clienteId = UUID.randomUUID();

      ResponseEntity<OrdenResponseDTO> createResp = restTemplate.postForEntity(
          BASE_URL,
          buildOrdenRequest(clienteId, productoId),
          OrdenResponseDTO.class);

      assertEquals(HttpStatus.CREATED, createResp.getStatusCode());
      UUID ordenId = createResp.getBody().id();

      // Confirmar
      ResponseEntity<OrdenResponseDTO> confirmarResp = restTemplate.postForEntity(
          BASE_URL + "/" + ordenId + "/confirmar",
          null,
          OrdenResponseDTO.class);

      assertEquals(HttpStatus.OK, confirmarResp.getStatusCode());
      assertEquals(EstadoOrden.CONFIRMADA, confirmarResp.getBody().estado());

      // Procesar pago
      PagarOrdenRequest pago = new PagarOrdenRequest("REF123456");
      ResponseEntity<OrdenResponseDTO> pagoResp = restTemplate.exchange(
          BASE_URL + "/" + ordenId + "/pago",
          HttpMethod.POST,
          new HttpEntity<>(pago),
          OrdenResponseDTO.class);

      assertEquals(HttpStatus.OK, pagoResp.getStatusCode());
      assertEquals(EstadoOrden.PAGO_PROCESADO, pagoResp.getBody().estado());

      // Marcar en preparación
      ResponseEntity<OrdenResponseDTO> preparacionResp = restTemplate.postForEntity(
          BASE_URL + "/" + ordenId + "/en-preparacion",
          null,
          OrdenResponseDTO.class);

      assertEquals(HttpStatus.OK, preparacionResp.getStatusCode());
      assertEquals(EstadoOrden.EN_PREPARACION, preparacionResp.getBody().estado());

      // Marcar enviada
      InfoEnvioRequest envio = new InfoEnvioRequest("1234567890", "DHL");
      ResponseEntity<OrdenResponseDTO> enviadaResp = restTemplate.exchange(
          BASE_URL + "/" + ordenId + "/enviada",
          HttpMethod.POST,
          new HttpEntity<>(envio),
          OrdenResponseDTO.class);

      assertEquals(HttpStatus.OK, enviadaResp.getStatusCode());
      assertEquals(EstadoOrden.ENVIADA, enviadaResp.getBody().estado());

      // Marcar entregada
      ResponseEntity<OrdenResponseDTO> entregadaResp = restTemplate.postForEntity(
          BASE_URL + "/" + ordenId + "/entregada",
          null,
          OrdenResponseDTO.class);

      assertEquals(HttpStatus.OK, entregadaResp.getStatusCode());
      assertEquals(EstadoOrden.ENTREGADA, entregadaResp.getBody().estado());
    }

    @Test
    @DisplayName("cancelar una orden antes del envío")
    void cancelarOrdenAntesDeEnvio_devuelveCancelada() {
      UUID ordenId = givenOrdenCreada();

      restTemplate.postForEntity(BASE_URL + "/" + ordenId + "/confirmar", null, OrdenResponseDTO.class);

      CancelarOrdenRequest cancelar = new CancelarOrdenRequest("Ya no necesito la orden");
      ResponseEntity<OrdenResponseDTO> cancelarResp = restTemplate.exchange(
          BASE_URL + "/" + ordenId + "/cancelar",
          HttpMethod.POST,
          new HttpEntity<>(cancelar),
          OrdenResponseDTO.class);

      assertEquals(HttpStatus.OK, cancelarResp.getStatusCode());
      assertEquals(EstadoOrden.CANCELADA, cancelarResp.getBody().estado());
    }
  }

  @Nested
  @DisplayName("Validaciones de payload (HTTP 400)")
  class Validaciones {

    @Test
    @DisplayName("no permite crear orden sin items (HTTP 400)")
    void crearOrdenSinItems_retorna400() {
      UUID clienteId = UUID.randomUUID();

      DireccionEnvioRequest direccion = new DireccionEnvioRequest(
          "Juan Pérez",
          "Calle Falsa 123",
          "Ciudad de México",
          "CDMX",
          "01234",
          "5512345678",
          "");

      OrdenRequest request = new OrdenRequest(clienteId, direccion, List.of());

      ResponseEntity<ApiError> response = restTemplate.exchange(
          BASE_URL,
          HttpMethod.POST,
          new HttpEntity<>(request),
          ApiError.class);

      assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
      assertNotNull(response.getBody());
      assertTrue(response.getBody().getMessage().toLowerCase().contains("al menos un ítem"));
    }

    @Test
    @DisplayName("no permite crear orden sin clienteId (HTTP 400)")
    void crearOrdenSinClienteId_retorna400() {
      UUID productoId = givenProductoEnCatalogo();

      DireccionEnvioRequest direccion = new DireccionEnvioRequest(
          "Juan Pérez",
          "Av. Universidad 3000",
          "Ciudad de México",
          "CDMX",
          "04510",
          "5512345678",
          "Dejar en portería");

      OrdenRequest request = new OrdenRequest(
          null,
          direccion,
          List.of(new ItemOrdenRequest(productoId.toString(), new BigDecimal("1"))));

      ResponseEntity<ApiError> response = restTemplate.exchange(
          BASE_URL,
          HttpMethod.POST,
          new HttpEntity<>(request),
          ApiError.class);

      assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
      assertNotNull(response.getBody());
      assertTrue(response.getBody().getMessage().toLowerCase().contains("clienteid"));
    }

    @Test
    @DisplayName("no permite crear orden con productoId no-UUID (HTTP 400)")
    void crearOrdenConProductoIdInvalido_retorna400() {
      UUID clienteId = UUID.randomUUID();

      DireccionEnvioRequest direccion = new DireccionEnvioRequest(
          "Juan Pérez",
          "Av. Universidad 3000",
          "Ciudad de México",
          "CDMX",
          "04510",
          "5512345678",
          "Dejar en portería");

      OrdenRequest request = new OrdenRequest(
          clienteId,
          direccion,
          List.of(new ItemOrdenRequest("NO-ES-UUID", new BigDecimal("1"))));

      ResponseEntity<ApiError> response = restTemplate.exchange(
          BASE_URL,
          HttpMethod.POST,
          new HttpEntity<>(request),
          ApiError.class);

      assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
      assertNotNull(response.getBody());
      assertTrue(response.getBody().getMessage().toLowerCase().contains("productoid"));
    }

    @Test
    @DisplayName("no permite crear orden con cantidad <= 0 (HTTP 400)")
    void crearOrdenConCantidadCero_retorna400() {
      UUID productoId = givenProductoEnCatalogo();
      UUID clienteId = UUID.randomUUID();

      DireccionEnvioRequest direccion = new DireccionEnvioRequest(
          "Juan Pérez",
          "Av. Universidad 3000",
          "Ciudad de México",
          "CDMX",
          "04510",
          "5512345678",
          "Dejar en portería");

      OrdenRequest request = new OrdenRequest(
          clienteId,
          direccion,
          List.of(new ItemOrdenRequest(productoId.toString(), new BigDecimal("0"))));

      ResponseEntity<ApiError> response = restTemplate.exchange(
          BASE_URL,
          HttpMethod.POST,
          new HttpEntity<>(request),
          ApiError.class);

      assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
      assertNotNull(response.getBody());
      assertTrue(response.getBody().getMessage().toLowerCase().contains("cantidad"));
    }

    @Test
    @DisplayName("no permite crear orden con código postal inválido (HTTP 400)")
    void crearOrdenConCodigoPostalInvalido_retorna400() {
      UUID productoId = givenProductoEnCatalogo();
      UUID clienteId = UUID.randomUUID();

      DireccionEnvioRequest direccion = new DireccionEnvioRequest(
          "Juan Pérez",
          "Av. Universidad 3000",
          "Ciudad de México",
          "CDMX",
          "ABCDE",
          "5512345678",
          "Dejar en portería");

      OrdenRequest request = new OrdenRequest(
          clienteId,
          direccion,
          List.of(new ItemOrdenRequest(productoId.toString(), new BigDecimal("1"))));

      ResponseEntity<ApiError> response = restTemplate.exchange(
          BASE_URL,
          HttpMethod.POST,
          new HttpEntity<>(request),
          ApiError.class);

      assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
      assertNotNull(response.getBody());
      assertTrue(response.getBody().getMessage().toLowerCase().contains("codigopostal"));
    }

    @Test
    @DisplayName("no permite procesar pago sin referencia (HTTP 400)")
    void procesarPagoSinReferencia_retorna400() {
      UUID ordenId = givenOrdenCreada();

      restTemplate.postForEntity(BASE_URL + "/" + ordenId + "/confirmar", null, OrdenResponseDTO.class);

      PagarOrdenRequest request = new PagarOrdenRequest("   ");

      ResponseEntity<ApiError> response = restTemplate.exchange(
          BASE_URL + "/" + ordenId + "/pago",
          HttpMethod.POST,
          new HttpEntity<>(request),
          ApiError.class);

      assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
      assertNotNull(response.getBody());
      assertTrue(response.getBody().getMessage().toLowerCase().contains("referenciapago"));
    }

    @Test
    @DisplayName("no permite cancelar con motivo corto (HTTP 400)")
    void cancelarConMotivoCorto_retorna400() {
      UUID ordenId = givenOrdenCreada();

      CancelarOrdenRequest request = new CancelarOrdenRequest("Muy corto");

      ResponseEntity<ApiError> response = restTemplate.exchange(
          BASE_URL + "/" + ordenId + "/cancelar",
          HttpMethod.POST,
          new HttpEntity<>(request),
          ApiError.class);

      assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
      assertNotNull(response.getBody());
      assertTrue(response.getBody().getMessage().toLowerCase().contains("motivo"));
    }

    @Test
    @DisplayName("no permite marcar enviada sin numeroGuia (HTTP 400)")
    void marcarEnviadaSinNumeroGuia_retorna400() {
      UUID ordenId = givenOrdenCreada();

      restTemplate.postForEntity(BASE_URL + "/" + ordenId + "/confirmar", null, OrdenResponseDTO.class);
      restTemplate.exchange(
          BASE_URL + "/" + ordenId + "/pago",
          HttpMethod.POST,
          new HttpEntity<>(new PagarOrdenRequest("REF-OK-123")),
          OrdenResponseDTO.class);
      restTemplate.postForEntity(BASE_URL + "/" + ordenId + "/en-preparacion", null, OrdenResponseDTO.class);

      InfoEnvioRequest request = new InfoEnvioRequest("   ", "DHL");

      ResponseEntity<ApiError> response = restTemplate.exchange(
          BASE_URL + "/" + ordenId + "/enviada",
          HttpMethod.POST,
          new HttpEntity<>(request),
          ApiError.class);

      assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
      assertNotNull(response.getBody());
      assertTrue(response.getBody().getMessage().toLowerCase().contains("numeroguia"));
    }
  }

  @Nested
  @DisplayName("Violaciones de lógica de negocio (HTTP 422)")
  class ReglasNegocio {

    @Test
    @DisplayName("no permite procesar pago si la orden no está CONFIRMADA (422)")
    void procesarPagoSinConfirmar_retorna422() {
      UUID ordenId = givenOrdenCreada();

      ResponseEntity<ApiError> response = restTemplate.exchange(
          BASE_URL + "/" + ordenId + "/pago",
          HttpMethod.POST,
          new HttpEntity<>(new PagarOrdenRequest("REF-OK-123")),
          ApiError.class);

      assertEquals(HTTP_422, response.getStatusCode().value());
      assertNotNull(response.getBody());
      assertTrue(response.getBody().getMessage().toLowerCase().contains("confirmada"));
    }

    @Test
    @DisplayName("no permite confirmar una orden que no está PENDIENTE (422)")
    void confirmarDosVeces_retorna422() {
      UUID ordenId = givenOrdenCreada();

      ResponseEntity<OrdenResponseDTO> ok = restTemplate.postForEntity(
          BASE_URL + "/" + ordenId + "/confirmar",
          null,
          OrdenResponseDTO.class);
      assertEquals(HttpStatus.OK, ok.getStatusCode());

      ResponseEntity<ApiError> response = restTemplate.exchange(
          BASE_URL + "/" + ordenId + "/confirmar",
          HttpMethod.POST,
          HttpEntity.EMPTY,
          ApiError.class);

      assertEquals(HTTP_422, response.getStatusCode().value());
      assertNotNull(response.getBody());
      assertTrue(response.getBody().getMessage().toLowerCase().contains("pendiente"));
    }

    @Test
    @DisplayName("no permite marcar en preparación si el pago no fue procesado (422)")
    void marcarEnPreparacionSinPago_retorna422() {
      UUID ordenId = givenOrdenCreada();

      restTemplate.postForEntity(BASE_URL + "/" + ordenId + "/confirmar", null, OrdenResponseDTO.class);

      ResponseEntity<ApiError> response = restTemplate.exchange(
          BASE_URL + "/" + ordenId + "/en-preparacion",
          HttpMethod.POST,
          HttpEntity.EMPTY,
          ApiError.class);

      assertEquals(HTTP_422, response.getStatusCode().value());
      assertNotNull(response.getBody());
      assertTrue(response.getBody().getMessage().toLowerCase().contains("pago"));
    }

    @Test
    @DisplayName("no permite marcar enviada si no está EN_PREPARACION (422)")
    void marcarEnviadaSinEnPreparacion_retorna422() {
      UUID ordenId = givenOrdenCreada();

      restTemplate.postForEntity(BASE_URL + "/" + ordenId + "/confirmar", null, OrdenResponseDTO.class);
      restTemplate.exchange(
          BASE_URL + "/" + ordenId + "/pago",
          HttpMethod.POST,
          new HttpEntity<>(new PagarOrdenRequest("REF-OK-123")),
          OrdenResponseDTO.class);

      ResponseEntity<ApiError> response = restTemplate.exchange(
          BASE_URL + "/" + ordenId + "/enviada",
          HttpMethod.POST,
          new HttpEntity<>(new InfoEnvioRequest("1234567890", "DHL")),
          ApiError.class);

      assertEquals(HTTP_422, response.getStatusCode().value());
      assertNotNull(response.getBody());
      assertTrue(response.getBody().getMessage().toLowerCase().contains("en_preparacion"));
    }

    @Test
    @DisplayName("no permite marcar entregada si no está ENVIADA o EN_TRANSITO (422)")
    void marcarEntregadaSinEnviada_retorna422() {
      UUID ordenId = givenOrdenCreada();

      restTemplate.postForEntity(BASE_URL + "/" + ordenId + "/confirmar", null, OrdenResponseDTO.class);
      restTemplate.exchange(
          BASE_URL + "/" + ordenId + "/pago",
          HttpMethod.POST,
          new HttpEntity<>(new PagarOrdenRequest("REF-OK-123")),
          OrdenResponseDTO.class);
      restTemplate.postForEntity(BASE_URL + "/" + ordenId + "/en-preparacion", null, OrdenResponseDTO.class);

      ResponseEntity<ApiError> response = restTemplate.exchange(
          BASE_URL + "/" + ordenId + "/entregada",
          HttpMethod.POST,
          HttpEntity.EMPTY,
          ApiError.class);

      assertEquals(HTTP_422, response.getStatusCode().value());
      assertNotNull(response.getBody());
      assertTrue(response.getBody().getMessage().toLowerCase().contains("enviada"));
    }

    @Test
    @DisplayName("no permite cancelar una orden que ya fue enviada (422)")
    void cancelarDespuesDeEnviada_retorna422() {
      UUID ordenId = givenOrdenCreada();

      restTemplate.postForEntity(BASE_URL + "/" + ordenId + "/confirmar", null, OrdenResponseDTO.class);
      restTemplate.exchange(
          BASE_URL + "/" + ordenId + "/pago",
          HttpMethod.POST,
          new HttpEntity<>(new PagarOrdenRequest("REF-OK-123")),
          OrdenResponseDTO.class);
      restTemplate.postForEntity(BASE_URL + "/" + ordenId + "/en-preparacion", null, OrdenResponseDTO.class);
      restTemplate.exchange(
          BASE_URL + "/" + ordenId + "/enviada",
          HttpMethod.POST,
          new HttpEntity<>(new InfoEnvioRequest("1234567890", "DHL")),
          OrdenResponseDTO.class);

      ResponseEntity<ApiError> response = restTemplate.exchange(
          BASE_URL + "/" + ordenId + "/cancelar",
          HttpMethod.POST,
          new HttpEntity<>(new CancelarOrdenRequest("Motivo válido de cancelación")),
          ApiError.class);

      assertEquals(HTTP_422, response.getStatusCode().value());
      assertNotNull(response.getBody());
      assertTrue(response.getBody().getMessage().toLowerCase().contains("no se puede cancelar"));
    }
  }

  @Nested
  @DisplayName("Recursos no encontrados (HTTP 404)")
  class NotFound {

    @Test
    @DisplayName("buscar orden por ID inexistente retorna 404")
    void buscarOrdenInexistente_retorna404() {
      UUID inexistente = UUID.randomUUID();

      ResponseEntity<ApiError> response = restTemplate.getForEntity(
          BASE_URL + "/" + inexistente,
          ApiError.class);

      assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
      assertNotNull(response.getBody());
      assertTrue(response.getBody().getMessage().toLowerCase().contains("orden no encontrada"));
    }

    @Test
    @DisplayName("confirmar orden inexistente retorna 404")
    void confirmarOrdenInexistente_retorna404() {
      UUID inexistente = UUID.randomUUID();

      ResponseEntity<ApiError> response = restTemplate.exchange(
          BASE_URL + "/" + inexistente + "/confirmar",
          HttpMethod.POST,
          HttpEntity.EMPTY,
          ApiError.class);

      assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
      assertNotNull(response.getBody());
      assertTrue(response.getBody().getMessage().toLowerCase().contains("orden no encontrada"));
    }

    @Test
    @DisplayName("crear orden con producto inexistente retorna 404")
    void crearOrdenConProductoInexistente_retorna404() {
      UUID clienteId = UUID.randomUUID();
      UUID productoInexistente = UUID.randomUUID();

      DireccionEnvioRequest direccion = new DireccionEnvioRequest(
          "Juan Pérez",
          "Av. Universidad 3000",
          "Ciudad de México",
          "CDMX",
          "04510",
          "5512345678",
          "Dejar en portería");

      OrdenRequest request = new OrdenRequest(
          clienteId,
          direccion,
          List.of(new ItemOrdenRequest(productoInexistente.toString(), new BigDecimal("1"))));

      ResponseEntity<ApiError> response = restTemplate.exchange(
          BASE_URL,
          HttpMethod.POST,
          new HttpEntity<>(request),
          ApiError.class);

      assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
      assertNotNull(response.getBody());
      assertTrue(response.getBody().getMessage().toLowerCase().contains("producto no encontrado"));
    }
  }
}
