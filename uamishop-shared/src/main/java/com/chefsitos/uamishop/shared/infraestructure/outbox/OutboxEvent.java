package com.chefsitos.uamishop.shared.infraestructure.outbox;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Getter;

@Entity
@Getter
@Table(name = "outbox_event")
public class OutboxEvent {
  @Id
  private UUID id;

  private String type;

  @Lob
  private String payload;

  private String exchange;

  @Column(name = "routing_key")
  private String routingKey;

  private int attempts = 0;

  private boolean processed = false;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @Column(name = "processed_at")
  private LocalDateTime processedAt;

  @Lob
  @Column(name = "last_error")
  private String lastError;

  protected OutboxEvent() {
  }

  public OutboxEvent(String type, String payload, String exchange, String routingKey) {
    this.id = UUID.randomUUID();
    this.type = type;
    this.payload = payload;
    this.exchange = exchange;
    this.routingKey = routingKey;
    this.createdAt = LocalDateTime.now();
  }

  public void markAsProcessed() {
    this.processed = true;
    this.processedAt = LocalDateTime.now();
  }

  public void markAsFailed(String error) {
    this.attempts++;
    this.lastError = error;
  }

  public boolean canRetry() {
    return this.attempts < 5 && !this.processed;
  }
}
