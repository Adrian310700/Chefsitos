package com.chefsitos.uamishop.shared.infraestructure.outbox;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Puerto para que los servicios de aplicación registren eventos en la tabla
 * Outbox.
 * Serializa automáticamente el payload a JSON usando ObjectMapper.
 * Se une a la transacción activa del caller (no abre transacción propia).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxStore {

  private final OutboxRepository outboxRepository;
  private final ObjectMapper objectMapper;

  /**
   * Registra un evento en la tabla Outbox dentro de la transacción activa.
   *
   * @param type       tipo del evento (ej: "ProductoComprado", "OrdenCreada")
   * @param payload    objeto a serializar como JSON (record, DTO, etc.)
   * @param exchange   exchange de destino en el broker
   * @param routingKey routing key para el enrutamiento del mensaje
   */
  public void save(String type, Object payload, String exchange, String routingKey) {
    try {
      String json = objectMapper.writeValueAsString(payload);
      OutboxEvent event = new OutboxEvent(type, json, exchange, routingKey);
      outboxRepository.save(event);
      log.debug("OutboxEvent registrado: type={}, exchange={}, routingKey={}, id={}",
          type, exchange, routingKey, event.getId());
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException("Error al serializar el payload del evento Outbox: " + e.getMessage(), e);
    }
  }
}
