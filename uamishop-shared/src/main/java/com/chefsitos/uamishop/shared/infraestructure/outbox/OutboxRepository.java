package com.chefsitos.uamishop.shared.infraestructure.outbox;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OutboxRepository extends JpaRepository<OutboxEvent, UUID> {

  @Query("SELECT e FROM OutboxEvent e WHERE e.processed = false AND e.attempts < 5 ORDER BY e.createdAt ASC")
  List<OutboxEvent> findUnprocessedEvents(Pageable pageable);
}
