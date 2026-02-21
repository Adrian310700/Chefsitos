package com.chefsitos.uamishop.ordenes.integration;

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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.chefsitos.uamishop.catalogo.domain.entity.Categoria;
import com.chefsitos.uamishop.catalogo.domain.valueObject.CategoriaId;
import com.chefsitos.uamishop.catalogo.repository.CategoriaJpaRepository;
import com.chefsitos.uamishop.catalogo.repository.ProductoJpaRepository;
import com.chefsitos.uamishop.catalogo.controller.dto.ProductoRequest;
import com.chefsitos.uamishop.catalogo.controller.dto.ProductoResponse;
import com.chefsitos.uamishop.ordenes.controller.dto.CancelarOrdenRequest;
import com.chefsitos.uamishop.ordenes.controller.dto.DireccionEnvioRequest;
import com.chefsitos.uamishop.ordenes.controller.dto.InfoEnvioRequest;
import com.chefsitos.uamishop.ordenes.controller.dto.OrdenRequest;
import com.chefsitos.uamishop.ordenes.controller.dto.OrdenRequest.ItemOrdenRequest;
import com.chefsitos.uamishop.ordenes.controller.dto.OrdenResponseDTO;
import com.chefsitos.uamishop.ordenes.controller.dto.PagarOrdenRequest;
import com.chefsitos.uamishop.ordenes.domain.enumeration.EstadoOrden;
import com.chefsitos.uamishop.ordenes.repository.OrdenJpaRepository;

/**
 * Pruebas de integración para el controlador de Órdenes.
 *
 * <p>
 * Se cubren los casos de creación, consulta y transición de estados de una
 * orden a través del API REST. Cada prueba se ejecuta con una base de datos
 * limpia gracias al método {@link #cleanUp()}. Para interactuar con la API se
 * emplea {@link TestRestTemplate}, que permite enviar peticiones HTTP a
 * nuestro servidor Spring Boot embebido.
 * </p>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class OrdenControllerIntegrationTest {

  private static final String BASE_URL = "/api/v1/ordenes";

  @Autowired
  private TestRestTemplate restTemplate;

  @Autowired
  private ProductoJpaRepository productoRepository;

  @Autowired
  private CategoriaJpaRepository categoriaRepository;

  @Autowired
  private OrdenJpaRepository ordenRepository;

  /**
   * Limpia todas las tablas relevantes entre pruebas para evitar que el estado
   * persista y afecte los siguientes casos. Este método se ejecuta después de
   * cada prueba gracias a la anotación {@link AfterEach}.
   */
  @AfterEach
  void cleanUp() {
    ordenRepository.deleteAll();
    productoRepository.deleteAll();
    categoriaRepository.deleteAll();
  }

  /**
   * Crea una categoría y un producto en la base de datos de pruebas utilizando
   * el endpoint de productos para que las órdenes tengan ítems válidos.
   *
   * @return el ID del producto recién creado.
   */
  private UUID givenProductoEnCatalogo() {
    // Creamos una categoría mínima válida en la base de datos utilizando el dominio
    UUID categoriaId = UUID.randomUUID();
    Categoria categoria = Categoria.crear(new CategoriaId(categoriaId), "Electrónicos", "Dispositivos electrónicos");
    categoriaRepository.save(categoria);

    // Construimos una solicitud de producto utilizando el controlador de productos
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

  /**
   * Prepara un cuerpo de solicitud de orden con datos consistentes para las
   * pruebas. Se requiere un ID de producto válido, que debe existir en la base
   * de datos antes de llamar a este método.
   */
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
      // La cabecera Location debe existir y contener el path del nuevo recurso
      String location = response.getHeaders().getFirst("Location");
      assertNotNull(location);
      assertTrue(location.contains("/api/v1/ordenes/"));

      // El cuerpo de la respuesta no debe ser nulo y debe reflejar el estado inicial
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
      // Preparamos una orden existente
      UUID productoId = givenProductoEnCatalogo();
      UUID clienteId = UUID.randomUUID();
      OrdenRequest orden = buildOrdenRequest(clienteId, productoId);
      restTemplate.postForEntity(BASE_URL, orden, OrdenResponseDTO.class);

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
      OrdenRequest orden = buildOrdenRequest(clienteId, productoId);
      ResponseEntity<OrdenResponseDTO> createResp = restTemplate.postForEntity(
          BASE_URL,
          orden,
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
      UUID productoId = givenProductoEnCatalogo();
      UUID clienteId = UUID.randomUUID();
      OrdenRequest orden = buildOrdenRequest(clienteId, productoId);
      ResponseEntity<OrdenResponseDTO> createResp = restTemplate.postForEntity(
          BASE_URL,
          orden,
          OrdenResponseDTO.class);
      UUID ordenId = createResp.getBody().id();

      // Confirmamos para que la orden esté en un estado intermedio
      restTemplate.postForEntity(
          BASE_URL + "/" + ordenId + "/confirmar",
          null,
          OrdenResponseDTO.class);

      // Cancelar con un motivo válido (>10 caracteres)
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
  @DisplayName("Validaciones de payload")
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
      // Lista de items vacía para invalidar la solicitud
      OrdenRequest request = new OrdenRequest(clienteId, direccion, List.of());
      ResponseEntity<com.chefsitos.uamishop.shared.ApiError> response = restTemplate.exchange(
          BASE_URL,
          HttpMethod.POST,
          new HttpEntity<>(request),
          com.chefsitos.uamishop.shared.ApiError.class);

      assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
      // La clase ApiError genera getters mediante Lombok. Usamos getMessage() para
      // obtener el detalle de validación y verificamos que mencione los ítems.
      assertNotNull(response.getBody());
      assertTrue(response.getBody().getMessage().toLowerCase().contains("al menos un ítem"));
    }
  }
}
