package com.chefsitos.uamishop.shared.infraestructure.inbox;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para consultar si un evento ya fue procesado en el Inbox.
 */
@Repository
public interface InboxRepository extends JpaRepository<InboxEvent, UUID> {
}
