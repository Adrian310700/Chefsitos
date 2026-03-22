package com.chefsitos.uamishop.ventas.api;

import com.chefsitos.uamishop.ventas.api.dto.CarritoDTO;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;

/**
 * Implementación de CarritoApi que consume el microservicio de Catálogo vía
 * HTTP.
 * Se activa cuando Carrito está externalizado (perfil distinto a
 * carrito-local).
 */
@Component
@Profile("carrito-remoto")
public class CarritoApiHttpClient implements CarritoApi {

  private final RestTemplate restTemplate;
  private final String carritoBaseUrl;

  public CarritoApiHttpClient(RestTemplate restTemplate, @Value("${carrito.service.url}") String carritoBaseUrl) {
    this.restTemplate = restTemplate;
    this.carritoBaseUrl = carritoBaseUrl;
  }

  @Override
  public CarritoDTO obtenerCarrito(UUID carritoId) {
    String url = carritoBaseUrl + "/api/v1/carritos/" + carritoId;
    ResponseEntity<CarritoDTO> response = restTemplate.getForEntity(url, CarritoDTO.class);

    if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
      throw new RuntimeException("Carrito no encontrado en el servicio de ventas: " + carritoId);
    }

    return response.getBody();
  }

  @Override
  public CarritoDTO completarCheckout(UUID carritoId) {
    String url = carritoBaseUrl + "/api/v1/carritos/" + carritoId + "/checkout/completar";
    ResponseEntity<CarritoDTO> response = restTemplate.getForEntity(url, CarritoDTO.class);

    if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
      throw new RuntimeException("No se pudo completar el checkout en el servicio de ventas: " + carritoId);
    }

    return response.getBody();
  }

  @Override
  public void validarCarritoEnCheckout(UUID carritoId) {
    // Reutiliza obtenerCarrito la validación de estado ocurre sobre el DTO obtenido
    CarritoDTO carrito = obtenerCarrito(carritoId);
    if (!"EN_CHECKOUT".equals(carrito.estado())) {
      throw new RuntimeException("El carrito " + carritoId + " debe estar EN_CHECKOUT para crear una orden");
    }
  }
}
