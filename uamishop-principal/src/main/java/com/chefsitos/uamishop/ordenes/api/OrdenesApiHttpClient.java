package com.chefsitos.uamishop.ordenes.api;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.chefsitos.uamishop.ordenes.api.dto.OrdenDTO;

@Component
public class OrdenesApiHttpClient implements OrdenesApi {

  private final RestTemplate restTemplate;
  private final String ordenesBaseUrl;

  public OrdenesApiHttpClient(
      RestTemplate restTemplate,
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

    if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
      throw new RuntimeException("No se pudo confirmar la orden: " + id);
    }

    return response.getBody();
  }

  @Override
  public OrdenDTO cancelarOrden(UUID id, String motivo) {
    String url = ordenesBaseUrl + "/api/v1/ordenes/" + id + "/cancelar";

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    HttpEntity<Map<String, String>> request = new HttpEntity<>(Map.of("motivo", motivo), headers);

    ResponseEntity<OrdenDTO> response = restTemplate.postForEntity(url, request, OrdenDTO.class);

    if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
      throw new RuntimeException("No se pudo cancelar la orden: " + id);
    }

    return response.getBody();
  }
}
