package com.chefsitos.uamishop.shared.infraestructure.outbox;

import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Relay agnóstico que recupera eventos no procesados de la tabla Outbox
 * y los publica vía el OutboxMessagePublisher.
 *
 * Se activa sólo cuando existe un bean OutboxMessagePublisher en el contexto
 * (cuando el microservicio provee una implementación concreta).
 *
 * Delega a OutboxEventProcessor para evitar el self-invocation problem
 * y garantizar transacciones cortas e independientes por evento.
 */
@Slf4j
@Component
@ConditionalOnBean(OutboxMessagePublisher.class)
@RequiredArgsConstructor
public class OutboxScheduler {

  private final OutboxRepository outboxRepository;
  private final OutboxEventProcessor eventProcessor;

  @Scheduled(fixedDelayString = "${outbox.scheduler.delay:5000}")
  public void processOutboxEvents() {
    Pageable batch = PageRequest.of(0, 5);
    List<OutboxEvent> pendingEvents = outboxRepository.findUnprocessedEvents(batch);

    if (pendingEvents.isEmpty()) {
      return;
    }

    log.info("Outbox Relay: procesando {} eventos pendientes", pendingEvents.size());

    for (OutboxEvent event : pendingEvents) {
      try {
        eventProcessor.processEvent(event.getId());
      } catch (Exception e) {
        log.error("Outbox Relay: error procesando evento id={}, type={}: {}",
            event.getId(), event.getType(), e.getMessage());
        try {
          eventProcessor.markAsFailed(event.getId(), e.getMessage());
        } catch (Exception markError) {
          log.error("Outbox Relay: error al marcar evento como fallido id={}: {}",
              event.getId(), markError.getMessage());
        }
      }
    }
  }
}
