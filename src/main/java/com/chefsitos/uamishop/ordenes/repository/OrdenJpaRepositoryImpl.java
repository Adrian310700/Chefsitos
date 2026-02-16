package com.chefsitos.uamishop.ordenes.repository;

import com.chefsitos.uamishop.ordenes.domain.aggregate.Orden;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementación en memoria del repositorio de Órdenes.
 */
@Repository
public class OrdenJpaRepositoryImpl implements OrdenJpaRepository {

  private final Map<UUID, Orden> store = new ConcurrentHashMap<>();

  @Override
  public Orden save(Orden orden) {
    store.put(orden.getId().valor(), orden);
    return orden;
  }

  @Override
  public Optional<Orden> findById(UUID id) {
    return Optional.ofNullable(store.get(id));
  }

  @Override
  public List<Orden> findAll() {
    return new ArrayList<>(store.values());
  }
}
