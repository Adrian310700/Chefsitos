package com.chefsitos.uamishop.ventas.api;

import com.chefsitos.uamishop.shared.enumeration.EstadoCarrito;
import com.chefsitos.uamishop.shared.exception.BusinessRuleException;
import com.chefsitos.uamishop.ventas.api.dto.CarritoDTO;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;

@Component
public class CarritoApiHttpClient implements CarritoApi {

  private final RestTemplate restTemplate;
  private final String carritoBaseUrl;

  public CarritoApiHttpClient(RestTemplate restTemplate, @Value("${carrito.service.url}") String carritoBaseUrl) {
    this.restTemplate = restTemplate;
    this.carritoBaseUrl = carritoBaseUrl;
  }

  // Implementacion de obtenerCarritoParaOrden
  public CarritoDTO obtenerCarritoParaOrden(UUID carritoId) {
    String url = carritoBaseUrl + "/api/v1/carritos/" + carritoId;
    ResponseEntity<CarritoDTO> response = restTemplate.getForEntity(url, CarritoDTO.class);

    // Validacion de que el carrito exista
    if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
      throw new BusinessRuleException("Carrito no encontrado en el servicio de ventas: " + carritoId);
    }

    CarritoDTO carrito = response.getBody();

    // Validacion de que el carrito este en estado EN_CHECKOUT
    if (!carrito.estado().equals(EstadoCarrito.EN_CHECKOUT.toString())) {
      throw new BusinessRuleException("El carrito " + carritoId + " debe estar EN_CHECKOUT para crear una orden");
    }

    return carrito;
  }
}
