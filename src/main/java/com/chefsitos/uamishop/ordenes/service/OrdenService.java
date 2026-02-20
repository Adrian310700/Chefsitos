package com.chefsitos.uamishop.ordenes.service;

import com.chefsitos.uamishop.ordenes.controller.dto.InfoEnvioRequest;
import com.chefsitos.uamishop.ordenes.controller.dto.OrdenRequest;
import com.chefsitos.uamishop.ordenes.domain.aggregate.Orden;
import com.chefsitos.uamishop.ordenes.domain.entity.ItemOrden;
import com.chefsitos.uamishop.ordenes.domain.valueObject.*;
import com.chefsitos.uamishop.ordenes.repository.OrdenJpaRepository;
import com.chefsitos.uamishop.shared.domain.valueObject.Money;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class OrdenService {

  private static final String MONEDA_DEFAULT = "MXN";

  private final OrdenJpaRepository ordenRepository;

  public OrdenService(OrdenJpaRepository ordenRepository) {
    this.ordenRepository = ordenRepository;
  }

  public Orden crear(OrdenRequest request) {

    DireccionEnvio direccion = new DireccionEnvio(
      request.direccion().nombreDestinatario(),
      request.direccion().calle(),
      request.direccion().ciudad(),
      request.direccion().estado(),
      request.direccion().codigoPostal(),
      request.direccion().pais(),
      request.direccion().telefono(),
      request.direccion().instrucciones()
    );

    List<ItemOrden> items = request.items().stream()
      .map(i -> new ItemOrden(
        i.productoId(),
        i.nombreProducto(),
        i.sku(),
        i.cantidad(),
        new Money(i.precioUnitario(), "MXN")
      ))
      .toList();

    Orden orden = new Orden(
      generarNumeroOrden(),
      new ClienteId(UUID.fromString(request.clienteId())),
      items,
      direccion
    );

    return ordenRepository.save(orden);
  }

  public Orden buscarPorId(UUID id) {
    return ordenRepository.findById(id)
      .orElseThrow(() -> new IllegalArgumentException("Orden no encontrada: " + id));
  }

  public Orden confirmar(UUID id) {
    Orden orden = buscarPorId(id);
    orden.confirmar();
    return ordenRepository.save(orden);
  }

  public Orden procesarPago(UUID id, String referenciaPago) {
    Orden orden = buscarPorId(id);
    orden.procesarPago(referenciaPago);
    return ordenRepository.save(orden);
  }

  public Orden marcarEnProceso(UUID id) {
    Orden orden = buscarPorId(id);
    orden.marcarEnProceso();
    return ordenRepository.save(orden);
  }

  public Orden marcarEnviada(UUID id, InfoEnvioRequest request) {
    Orden orden = buscarPorId(id);
    orden.marcarEnviada(request.numeroGuia());
    return ordenRepository.save(orden);
  }

  public Orden marcarEntregada(UUID id) {
    Orden orden = buscarPorId(id);
    orden.marcarEntregada();
    return ordenRepository.save(orden);
  }

  public Orden cancelar(UUID id, String motivo) {
    Orden orden = buscarPorId(id);
    orden.cancelar(motivo);
    return ordenRepository.save(orden);
  }

  private String generarNumeroOrden() {
    return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
  }
}
