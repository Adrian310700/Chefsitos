package com.chefsitos.uamishop.ordenes.api;

import java.util.List;
import java.util.UUID;
import com.chefsitos.uamishop.ordenes.api.dto.OrdenDTO;

/**
 * API pública del módulo Ordenes.
 * ÚNICO punto de entrada para otros módulos que necesiten interactuar con
 * Órdenes.
 *
 * Las clases internas (domain, service, repository) NO deben ser accedidas
 * desde fuera de este módulo. Solo se expone esta interfaz y los DTOs en el
 * paquete api/dto.
 */
public interface OrdenesApi {

  /**
   * Busca una orden por su ID.
   *
   * @param id UUID de la orden
   * @return DTO con la información pública de la orden
   */
  OrdenDTO buscarPorId(UUID id);

  /**
   * Retorna todas las órdenes registradas.
   *
   * @return lista de DTOs de órdenes
   */
  List<OrdenDTO> buscarTodas();

  /**
   * Confirma una orden en estado PENDIENTE.
   *
   * @param id UUID de la orden
   * @return DTO actualizado de la orden
   */
  OrdenDTO confirmarOrden(UUID id);

  /**
   * Cancela una orden que aún no ha sido enviada ni entregada.
   *
   * @param id     UUID de la orden
   * @param motivo motivo de cancelación (mínimo 10 caracteres)
   * @return DTO actualizado de la orden
   */
  OrdenDTO cancelarOrden(UUID id, String motivo);
}
