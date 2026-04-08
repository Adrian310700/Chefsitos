package com.chefsitos.uamishop.catalogo.api;

import com.chefsitos.uamishop.catalogo.api.dto.ProductoDTO;
import com.chefsitos.uamishop.shared.exception.ServiceUnavailableException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

@Component
public class CatalogoApiHttpClient implements CatalogoApi {

  private final RestTemplate restTemplate;
  private final String catalogoBaseUrl;

  public CatalogoApiHttpClient(RestTemplate restTemplate,
      @Value("${catalogo.service.url}") String catalogoBaseUrl) {
    this.restTemplate = restTemplate;
    this.catalogoBaseUrl = catalogoBaseUrl;
  }

  @CircuitBreaker(name = "catalogoService", fallbackMethod = "fallbackMethodBuscarPorId")
  public ProductoDTO buscarPorId(UUID id) {
    String url = catalogoBaseUrl + "/api/v1/productos/" + id;
    ResponseEntity<ProductoDTO> response = restTemplate.getForEntity(url, ProductoDTO.class);

    if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
      throw new RuntimeException("Producto no encontrado en el servicio de catálogo: " + id);
    }

    return response.getBody();
  }

  public ProductoDTO fallbackMethodBuscarPorId(UUID id, Exception ex) {
    throw new ServiceUnavailableException(
        "catalogo", "Servicio de catalogo no disponible temporalmente.", ex);
  }

}
