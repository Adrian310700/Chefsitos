package com.chefsitos.uamishop.catalogo.api;

import com.chefsitos.uamishop.catalogo.api.dto.ProductoDTO;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

/**
 * Implementación de CatalogoApi que consume el microservicio de Catálogo vía
 * HTTP.
 * Se activa únicamente con el perfil "catalogo-remoto", cuando el catálogo
 * está externalizado como microservicio independiente.
 */
@Component
@Profile("catalogo-remoto")
public class CatalogoApiHttpClient implements CatalogoApi {

  private final RestTemplate restTemplate;
  private final String catalogoBaseUrl;

  public CatalogoApiHttpClient(RestTemplate restTemplate,
      @Value("${catalogo.service.url}") String catalogoBaseUrl) {
    this.restTemplate = restTemplate;
    this.catalogoBaseUrl = catalogoBaseUrl;
  }

  public ProductoDTO buscarPorId(UUID id) {
    String url = catalogoBaseUrl + "/api/v1/productos/" + id;
    ResponseEntity<ProductoDTO> response = restTemplate.getForEntity(url, ProductoDTO.class);

    if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
      throw new RuntimeException("Producto no encontrado en el servicio de catálogo: " + id);
    }

    return response.getBody();
  }
}
