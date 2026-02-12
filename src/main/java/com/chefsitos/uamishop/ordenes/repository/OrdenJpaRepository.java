package com.chefsitos.uamishop.ordenes.repository;

import com.chefsitos.uamishop.ordenes.domain.aggregate.Orden;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio de Órdenes (contrato).
 */
public interface OrdenJpaRepository {

  /**
   * Guarda una orden (creación o actualización).
   *
   * @param orden agregado Orden a persistir
   * @return orden persistida
   */
  Orden save(Orden orden);

  /**
   * Busca una orden por UUID.
   *
   * @param id UUID de la orden
   * @return Optional con la orden si existe
   */
  Optional<Orden> findById(UUID id);

  /**
   * Devuelve todas las órdenes persistidas.
   *
   * @return lista de órdenes
   */
  List<Orden> findAll();
}
