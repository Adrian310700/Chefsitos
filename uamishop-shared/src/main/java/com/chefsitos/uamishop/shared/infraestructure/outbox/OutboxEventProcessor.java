package com.chefsitos.uamishop.shared.infraestructure.outbox;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Servicio transaccional de apoyo para el OutboxScheduler.
 * Existe como bean separado para evitar el self-invocation problem
 * de @Transactional.
 * Cada operación abre su propia transacción corta (REQUIRES_NEW).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxEventProcessor {

  private final OutboxRepository outboxRepository;
  private final OutboxMessagePublisher messagePublisher;

  /**
   * Procesa un evento Outbox en una transacción independiente:
   * 1. Recupera el evento de la BD
   * 2. Publica el mensaje vía el broker
   * 3. Marca el evento como procesado
   *
   * Si falla la publicación, la excepción se propaga al scheduler.
   */
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void processEvent(UUID eventId) {
    OutboxEvent event = outboxRepository.findById(eventId)
        .orElseThrow(() -> new IllegalStateException("OutboxEvent no encontrado: " + eventId));

    messagePublisher.publish(event);
    event.markAsProcessed();
    outboxRepository.save(event);

    log.debug("OutboxEvent procesado: id={}, type={}", event.getId(), event.getType());
  }

  /**
   * Marca un evento como fallido en una transacción independiente.
   * Incrementa el contador de intentos y registra el error.
   */
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void markAsFailed(UUID eventId, String error) {
    OutboxEvent event = outboxRepository.findById(eventId)
        .orElseThrow(() -> new IllegalStateException("OutboxEvent no encontrado: " + eventId));

    event.markAsFailed(error);
    outboxRepository.save(event);

    log.warn("OutboxEvent fallido: id={}, type={}, attempts={}, error={}",
        event.getId(), event.getType(), event.getAttempts(), error);
  }
}
