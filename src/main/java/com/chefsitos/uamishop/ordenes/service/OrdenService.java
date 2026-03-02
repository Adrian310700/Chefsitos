package com.chefsitos.uamishop.ordenes.service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chefsitos.uamishop.ordenes.controller.dto.OrdenRequest;
import com.chefsitos.uamishop.ordenes.controller.dto.OrdenResponseDTO;
import com.chefsitos.uamishop.ordenes.domain.aggregate.Orden;
import com.chefsitos.uamishop.ordenes.domain.entity.ItemOrden;
import com.chefsitos.uamishop.ordenes.domain.enumeration.EstadoPago;
import com.chefsitos.uamishop.ordenes.domain.valueObject.*;
import com.chefsitos.uamishop.ordenes.repository.OrdenJpaRepository;
import com.chefsitos.uamishop.shared.domain.valueObject.CarritoId;
import com.chefsitos.uamishop.shared.domain.valueObject.ClienteId;
import com.chefsitos.uamishop.shared.domain.valueObject.Money;
import com.chefsitos.uamishop.shared.domain.valueObject.ProductoId;
import com.chefsitos.uamishop.shared.exception.ResourceNotFoundException;
import com.chefsitos.uamishop.ventas.api.dto.CarritoDTO;
import com.chefsitos.uamishop.ventas.api.dto.ItemCarritoDTO;
import com.chefsitos.uamishop.ventas.api.CarritoApi;
import com.chefsitos.uamishop.catalogo.api.dto.ProductoDTO;
import com.chefsitos.uamishop.catalogo.api.ProductoApi;

// PENDIENTE – Ventas: CarritoService es una clase INTERNA del módulo ventas.

// Debe exponerse a través de una VentasApi para respetar la arquitectura modular.
// Temporalmente comentado hasta que el módulo ventas exponga su API pública.
//
// import com.chefsitos.uamishop.ventas.service.CarritoService;
// import com.chefsitos.uamishop.ventas.domain.aggregate.Carrito;
// import com.chefsitos.uamishop.ventas.domain.entity.ItemCarrito;

import com.chefsitos.uamishop.ordenes.api.OrdenesApi;
import com.chefsitos.uamishop.ordenes.api.dto.OrdenDTO;

@Service
public class OrdenService implements OrdenesApi {

  private final OrdenJpaRepository ordenRepository;
  private final ProductoApi productoApi;

  // PENDIENTE – descomentar cuando VentasApi esté disponible:
  // private final VentasApi ventasApi;

  public OrdenService(OrdenJpaRepository ordenRepository, ProductoApi productoApi) {
    this.ordenRepository = ordenRepository;
    this.productoApi = productoApi;
  }
  // Implementación de OrdenesApi (API pública del módulo)

  @Override
  public OrdenDTO buscarPorId(UUID id) {
    Orden orden = obtenerOrden(id);
    return OrdenDTO.from(orden);
  }

  @Override
  public List<OrdenDTO> buscarTodas() {
    return ordenRepository.findAll().stream()
        .map(OrdenDTO::from)
        .collect(Collectors.toList());
  }

  private final ProductoApi productoService;
  @Override
  @Transactional // Necesario para evitar LazyInitializationException
  public OrdenDTO confirmarOrden(UUID id) {
    Orden orden = obtenerOrden(id);
    orden.confirmar();
    return OrdenDTO.from(ordenRepository.save(orden));
  }

  @Override
  @Transactional // Necesario para evitar LazyInitializationException
  public OrdenDTO cancelarOrden(UUID id, String motivo) {
    Orden orden = obtenerOrden(id);
    orden.cancelar(motivo);
    return OrdenDTO.from(ordenRepository.save(orden));
  }
  // Métodos internos usados por OrdenController (REST)

  private final CarritoApi carritoService;

  public OrdenResponseDTO crear(OrdenRequest request) {
    DireccionEnvio direccion = new DireccionEnvio(
        request.direccion().nombreDestinatario(),
        request.direccion().calle(),
        request.direccion().ciudad(),
        request.direccion().estado(),
        request.direccion().codigoPostal(),
        "México",
        request.direccion().telefono(),
        request.direccion().instrucciones());

    List<ItemOrden> items = request.items().stream()
        .map(i -> {
          ProductoDTO producto = productoApi.buscarPorId(UUID.fromString(i.productoId()));
          return new ItemOrden(
              ProductoId.of(i.productoId()),
              producto.nombreProducto(),
              producto.nombreProducto(),
              i.cantidad().intValue(),
              new Money(producto.precio(), producto.moneda()));
        })
        .toList();

    ResumenPago resumenPendiente = new ResumenPago("PENDIENTE", null, EstadoPago.PENDIENTE, null);
    Orden orden = Orden.crear(new ClienteId(request.clienteId()), items, direccion, resumenPendiente);
    return mapToResponseDTO(ordenRepository.save(orden));
  }

  /**
   * PENDIENTE: Crear orden desde carrito.
   *
   * Este método depende de CarritoService (clase interna del módulo ventas).
   * Debe refactorizarse para consumir VentasApi cuando esté disponible.
   *
   * Código a implementar cuando VentasApi exista:
   *
   * <pre>
   * public OrdenResponseDTO crearDesdeCarrito(CarritoId carritoId, DireccionEnvio direccionEnvio) {
   *   // CarritoResumen carrito = ventasApi.obtenerCarrito(carritoId);
   *   // ClienteId clienteOrden =
   *   // ClienteId.of(carrito.clienteId().valor().toString());
   *   // List<ItemOrden> itemsOrden = carrito.items().stream()
   *   // .map(item -> new ItemOrden(
   *   // ProductoId.of(item.productoId().toString()),
   *   // item.nombreProducto(), item.sku(),
   *   // item.cantidad(), item.precioUnitario()))
   *   // .collect(Collectors.toList());
   *   // ResumenPago resumenPendiente = new ResumenPago("PENDIENTE", null,
   *   // EstadoPago.PENDIENTE, null);
   *   // Orden nuevaOrden = Orden.crear(clienteOrden, itemsOrden, direccionEnvio,
   *   // resumenPendiente);
   *   // nuevaOrden = ordenRepository.save(nuevaOrden);
   *   // ventasApi.completarCheckout(carritoId.getValue());
   *   // return mapToResponseDTO(nuevaOrden);
   * }
   * </pre>
   */
  public OrdenResponseDTO crearDesdeCarrito(CarritoId carritoId, DireccionEnvio direccionEnvio) {
    CarritoDTO carrito = carritoService.obtenerCarrito(carritoId.getValue());

    // Validación: el carrito debe estar en EN_CHECKOUT
    if (!"EN_CHECKOUT".equals(carrito.estado())) {
      throw new IllegalStateException(
          "El carrito debe estar en checkout para crear una orden");
    }

    ClienteId clienteOrden = ClienteId.of(carrito.clienteId().toString());

    List<ItemOrden> itemsOrden = carrito.items().stream()
        .map(OrdenService::mapItemCarritoToItemOrden)
        .collect(Collectors.toList());

    ResumenPago resumenPendiente = new ResumenPago(
        "PENDIENTE", null, EstadoPago.PENDIENTE, null);

    Orden nuevaOrden = Orden.crear(clienteOrden, itemsOrden, direccionEnvio, resumenPendiente);
    nuevaOrden = ordenRepository.save(nuevaOrden);
    carritoService.completarCheckout(carritoId.getValue());
    return mapToResponseDTO(nuevaOrden);
    throw new UnsupportedOperationException(
        "Pendiente: requiere VentasApi (API pública del módulo ventas).");
  }

  private Orden obtenerOrden(UUID id) {
    return ordenRepository.findById(new OrdenId(id))
        .orElseThrow(() -> new ResourceNotFoundException("Orden no encontrada con ID: " + id));
  }

  public OrdenResponseDTO buscarPorIdResponse(UUID id) {
    return mapToResponseDTO(obtenerOrden(id));
  }

  public List<OrdenResponseDTO> buscarTodasResponse() {
    return ordenRepository.findAll().stream()
        .map(OrdenService::mapToResponseDTO)
        .collect(Collectors.toList());
  }

  public OrdenResponseDTO confirmar(UUID id) {
    Orden orden = obtenerOrden(id);
    orden.confirmar();
    return mapToResponseDTO(ordenRepository.save(orden));
  }

  public OrdenResponseDTO procesarPago(UUID id, String referenciaPago) {
    Orden orden = obtenerOrden(id);
    orden.procesarPago(referenciaPago);
    return mapToResponseDTO(ordenRepository.save(orden));
  }

  public OrdenResponseDTO marcarEnProceso(UUID id) {
    Orden orden = obtenerOrden(id);
    orden.marcarEnProceso();
    return mapToResponseDTO(ordenRepository.save(orden));
  }

  public OrdenResponseDTO marcarEnviada(UUID id, InfoEnvio infoEnvio) {
    Orden orden = obtenerOrden(id);
    orden.marcarEnviada(infoEnvio);
    return mapToResponseDTO(ordenRepository.save(orden));
  }

  public OrdenResponseDTO marcarEntregada(UUID id) {
    Orden orden = obtenerOrden(id);
    orden.marcarEntregada();
    return mapToResponseDTO(ordenRepository.save(orden));
  }

  public OrdenResponseDTO cancelar(UUID id, String motivo) {
    Orden orden = obtenerOrden(id);
    orden.cancelar(motivo);
    return mapToResponseDTO(ordenRepository.save(orden));
  }

  // Helpers
  public static ItemOrden mapItemCarritoToItemOrden(ItemCarritoDTO item) {
    ProductoId productoId = ProductoId.of(item.productoId().toString());
    return new ItemOrden(
        productoId,
        item.nombreProducto(),
        item.sku(), // sku: workaround hasta tener campo propio en catálogo
        item.cantidad(),
        new Money(item.precioUnitario(), item.moneda()));
  }

  public static OrdenResponseDTO mapToResponseDTO(Orden orden) {
    return new OrdenResponseDTO(
        orden.getId().valor(),
        orden.getNumeroOrden(),
        orden.getClienteId().valor(),
        orden.getEstado(),
        orden.getTotal().cantidad(),
        orden.getTotal().moneda(),
        orden.getDireccionEnvio().calle() + ", " + orden.getDireccionEnvio().ciudad());
  }
}
