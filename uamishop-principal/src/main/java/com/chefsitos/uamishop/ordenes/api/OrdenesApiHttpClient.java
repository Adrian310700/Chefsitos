package com.chefsitos.uamishop.ordenes.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import java.util.List;
import java.util.UUID;
import java.util.Arrays;
import java.util.Collections;
import com.chefsitos.uamishop.ordenes.api.dto.OrdenDTO;

@Component
public class OrdenesApiHttpClient implements OrdenesApi {

  private final RestTemplate restTemplate;
  private final String ordenesBaseUrl;

  public OrdenesApiHttpClient(RestTemplate restTemplate,
                              @Value("${ordenes.service.url}") String ordenesBaseUrl) {
    this.restTemplate = restTemplate;
    this.ordenesBaseUrl = ordenesBaseUrl;
  }

  @Override
  public OrdenDTO buscarPorId(UUID id) {
    String url = ordenesBaseUrl + "/api/v1/ordenes/" + id;
    ResponseEntity<OrdenDTO> response = restTemplate.getForEntity(url, OrdenDTO.class);
    if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
      return null;
    }
    return response.getBody();
  }

  @Override
  public List<OrdenDTO> buscarTodas() {
    String url = ordenesBaseUrl + "/api/v1/ordenes";
    ResponseEntity<OrdenDTO[]> response = restTemplate.getForEntity(url, OrdenDTO[].class);
    if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
      return Collections.emptyList();
    }
    return Arrays.asList(response.getBody());
  }

  @Override
  public OrdenDTO confirmarOrden(UUID id) {
    String url = ordenesBaseUrl + "/api/v1/ordenes/" + id + "/confirmar";
    ResponseEntity<OrdenDTO> response = restTemplate.postForEntity(url, null, OrdenDTO.class);
    return response.getBody();
  }

  @Override
  public OrdenDTO cancelarOrden(UUID id, String motivo) {
    String url = ordenesBaseUrl + "/api/v1/ordenes/" + id + "/cancelar?motivo=" + motivo;
    ResponseEntity<OrdenDTO> response = restTemplate.postForEntity(url, null, OrdenDTO.class);
    return response.getBody();
  }
}
