package com.chefsitos.uamishop.shared.infraestructure.outbox;

/**
 * Abstracción agnóstica del broker de mensajería.
 */
public interface OutboxMessagePublisher {

  /**
   * Publica un evento Outbox en el broker de mensajería.
   * 
   * @param event el evento a publicar con su exchange, routingKey y payload
   */
  void publish(OutboxEvent event);
}
