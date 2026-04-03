package com.chefsitos.uamishop.shared.infraestructure.outbox;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "outbox_event")
public class OutboxEvent {
  @Id
  private UUID id;

  @Column(name = "aggregate_type", nullable = false)
  private String aggregateType;

  @Column(name = "aggregate_id", nullable = false)
  private String aggregateId;

  @Column(nullable = false)
  private String type;

  @Lob
  @Column(nullable = false)
  private String payload;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(nullable = false)
  private Boolean processed = false;

  @Column(name = "processed_at")
  private LocalDateTime processedAt;

  protected OutboxEvent() {
  }

  public OutboxEvent(UUID id, String aggregateType, String aggregateId,
      String type, String payload) {
    this.id = id;
    this.aggregateType = aggregateType;
    this.aggregateId = aggregateId;
    this.type = type;
    this.payload = payload;
    this.createdAt = LocalDateTime.now();
    this.processed = false;
  }
}
