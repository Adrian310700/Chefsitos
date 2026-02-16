package com.chefsitos.uamishop.ordenes.service;

import java.util.List;
import java.util.UUID;

/**
 * DTO que representa el comando para crear una Orden.
 * Pertenece a la capa de aplicación (service).
 */
public record OrdenRequest(

  // Datos generales de la orden
  String numeroOrden,
  UUID clienteId,

  // Datos de dirección de envío
  String nombreDestinatario,
  String calle,
  String ciudad,
  String estado,
  String codigoPostal,
  String pais,
  String telefono,
  String instrucciones,

  // Lista de productos de la orden
  List<ItemOrdenRequest> items

) {}
