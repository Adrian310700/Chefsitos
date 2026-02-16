package com.chefsitos.uamishop.ordenes.service;

import com.chefsitos.uamishop.ordenes.domain.aggregate.Orden;
import com.chefsitos.uamishop.ordenes.domain.entity.ItemOrden;
import com.chefsitos.uamishop.ordenes.domain.valueObject.*;
import com.chefsitos.uamishop.ordenes.repository.OrdenJpaRepository;
import com.chefsitos.uamishop.shared.domain.valueObject.Money;
import com.chefsitos.uamishop.ventas.domain.valueObject.CarritoId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Servicio de aplicación para el Bounded Context "Órdenes".
 */
@Service
@Transactional
public class OrdenService {

  private final OrdenJpaRepository ordenRepository;

  public OrdenService(OrdenJpaRepository ordenRepository) {
    this.ordenRepository = ordenRepository;
  }

  /**
   * Crea una nueva orden a partir del request.
   * Construye DireccionEnvio e ItemOrden desde el request
   * Crea la orden
   * Persiste y devuelve el agregado Orden
   */
  public Orden crear(OrdenRequest request) {
    if (request == null) {
      throw new IllegalArgumentException("OrdenRequest no puede ser null");
    }

    DireccionEnvio direccion = new DireccionEnvio(
        request.nombreDestinatario(),
        request.calle(),
        request.ciudad(),
        request.estado(),
        request.codigoPostal(),
        request.pais(),
        request.telefono(),
        request.instrucciones());

    List<ItemOrden> items = request.items().stream()
        .map(i -> new ItemOrden(
            ItemOrdenId.generar(),
            i.productoId(),
            i.nombreProducto(),
            i.sku(),
            i.cantidad(),
             new Money(i.precioUnitario(),"MXN")))

      .toList();

    Orden orden = new Orden(
        OrdenId.generar(),
        request.numeroOrden(),
        new ClienteId(request.clienteId()),
        items,
        direccion);

    return ordenRepository.save(orden);
  }

  /**
   * Crea una orden desde un carrito.
   * Obtiene el carrito vía CarritoService
   * Convierte ítems a ItemOrden
   * Crea la orden, persiste
   * Llama a completarCheckout del carrito
   *
   * Estado actual:
   * Pendiente porque NO existe CarritoService
   * Se deja el método con la firma requerida para integrarlo después.
   */
  public Orden crearDesdeCarrito(CarritoId carritoId, DireccionEnvio direccionEnvio) {
    throw new UnsupportedOperationException(
        "Pendiente: requiere CarritoService (módulo ventas) para implementar crearDesdeCarrito.");
  }

  /**
   * Busca una orden por UUID. Lanza excepción si no existe.
   */
  @Transactional(readOnly = true)
  public Orden buscarPorId(UUID id) {
    if (id == null) {
      throw new IllegalArgumentException("El id no puede ser null");
    }
    return ordenRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Orden no encontrada: " + id));
  }

  /**
   * Devuelve todas las órdenes persistidas.
   */
  @Transactional(readOnly = true)
  public List<Orden> buscarTodas() {
    return ordenRepository.findAll();
  }

  /**
   * Confirma una orden (solo si está en PENDIENTE; lo valida el agregado).
   */
  public Orden confirmar(UUID id) {
    Orden orden = buscarPorId(id);
    orden.confirmar();
    return ordenRepository.save(orden);
  }

  /**
   * Procesa el pago de una orden (solo si está CONFIRMADA; lo valida el
   * agregado).
   */
  public Orden procesarPago(UUID id, String referenciaPago) {
    if (referenciaPago == null || referenciaPago.isBlank()) {
      throw new IllegalArgumentException("La referenciaPago es obligatoria");
    }
    Orden orden = buscarPorId(id);
    orden.procesarPago(referenciaPago);
    return ordenRepository.save(orden);
  }

  /**
   * Marca una orden en proceso (solo si está PAGO_PROCESADO; lo valida el
   * agregado).
   */
  public Orden marcarEnProceso(UUID id) {
    Orden orden = buscarPorId(id);
    orden.marcarEnProceso();
    return ordenRepository.save(orden);
  }

  /**
   * Marca una orden como enviada.
   * InfoEnvio, pero tu agregado Orden expone:
   * marcarEnviada(String guiaEnvio)
   * Por eso aquí se adapta usando infoEnvio.numeroGuia().
   */
  public Orden marcarEnviada(UUID id, InfoEnvio infoEnvio) {
    if (infoEnvio == null) {
      throw new IllegalArgumentException("infoEnvio es obligatorio");
    }
    Orden orden = buscarPorId(id);
    orden.marcarEnviada(infoEnvio.numeroGuia());
    return ordenRepository.save(orden);
  }

  /**
   * Marca una orden como entregada (solo si está ENVIADA; lo valida el agregado).
   */
  public Orden marcarEntregada(UUID id) {
    Orden orden = buscarPorId(id);
    orden.marcarEntregada();
    return ordenRepository.save(orden);
  }

  /**
   * Cancela una orden con un motivo (restricciones las valida el agregado).
   */
  public Orden cancelar(UUID id, String motivo) {
    if (motivo == null || motivo.isBlank()) {
      throw new IllegalArgumentException("El motivo es obligatorio");
    }
    Orden orden = buscarPorId(id);
    orden.cancelar(motivo);
    return ordenRepository.save(orden);
  }
}
