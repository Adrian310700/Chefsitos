package com.chefsitos.uamishop.shared.infraestructure.inbox;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Tabla generica para implementar el Patron Inbox / Consumidor Idempotente.
 * Almacena el ID de los eventos ya procesados para evitar procesarlos 2 veces.
 */
@Entity
@Table(name = "inbox_events")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class InboxEvent {

  @Id
  @Column(name = "event_id", updatable = false, nullable = false)
  private UUID eventId;

  @Column(name = "processed_at", nullable = false)
  private Instant processedAt;

  public static InboxEvent from(UUID eventId) {
    return new InboxEvent(eventId, Instant.now());
  }
}
