package com.chefsitos.uamishop.shared.infraestructure.outbox;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springdoc.core.converters.models.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OutboxRepository extends JpaRepository<OutboxEvent, UUID> {
  @Query("SELECT e FROM OutboxEvent e WHERE e.processed = false ORDER BY e.createdAt ASC")
  List<OutboxEvent> findUnprocessedEvents(Pageable pageable);

  @Modifying
  @Query("UPDATE OutboxEvent e SET e.processed = true, e.processedAt = :processedAt WHERE e.id = :id")
  void markAsProcessed(@Param("id") UUID id, @Param("processedAt") LocalDateTime processedAt);
}
