package com.chefsitos.uamishop.ordenes.service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.UUID;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import com.chefsitos.uamishop.catalogo.api.dto.ProductoDTO;
import com.chefsitos.uamishop.catalogo.api.ProductoApi;
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

@Service
@AllArgsConstructor
public class OrdenService {

  private final OrdenJpaRepository ordenRepository;

  private final ProductoApi productoService;

  private final CarritoApi carritoService;

  public OrdenResponseDTO crear(OrdenRequest request) {
    DireccionEnvio direccion = new DireccionEnvio(
        request.direccion().nombreDestinatario(),
        request.direccion().calle(),
        request.direccion().ciudad(),
        request.direccion().estado(),
        request.direccion().codigoPostal(),
        "México", // RN-VO-04
        request.direccion().telefono(),
        request.direccion().instrucciones());

    List<ItemOrden> items = request.items().stream()
        .map(i -> {
          ProductoDTO producto = productoService.buscarPorId(UUID.fromString(i.productoId()));
          return new ItemOrden(
              ProductoId.of(i.productoId()),
              producto.nombreProducto(),
              producto.nombreProducto(), // sku: alias hasta tener campo propio
              i.cantidad().intValue(),
              new Money(producto.precio(), producto.moneda()));
        })
        .toList();

    ResumenPago resumenPendiente = new ResumenPago(
        "PENDIENTE", null, EstadoPago.PENDIENTE, null);

    Orden orden = Orden.crear(
        new ClienteId(request.clienteId()), items, direccion, resumenPendiente);
    return mapToResponseDTO(ordenRepository.save(orden));
  }

  public OrdenResponseDTO crearDesdeCarrito(CarritoId carritoId, DireccionEnvio direccionEnvio) {
    CarritoDTO carrito = carritoService.obtenerCarrito(carritoId.getValue());
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
  }

  public Orden buscarPorId(UUID id) {
    return ordenRepository.findById(new OrdenId(id))
        .orElseThrow(() -> new ResourceNotFoundException("Orden no encontrada con ID: " + id));
  }

  public List<OrdenResponseDTO> buscarTodas() {
    return ordenRepository.findAll().stream()
        .map(OrdenService::mapToResponseDTO)
        .collect(Collectors.toList());
  }

  public OrdenResponseDTO confirmar(UUID id) {
    Orden orden = buscarPorId(id);
    orden.confirmar();
    return mapToResponseDTO(ordenRepository.save(orden));
  }

  public OrdenResponseDTO procesarPago(UUID id, String referenciaPago) {
    Orden orden = buscarPorId(id);
    orden.procesarPago(referenciaPago);
    return mapToResponseDTO(ordenRepository.save(orden));
  }

  public OrdenResponseDTO marcarEnProceso(UUID id) {
    Orden orden = buscarPorId(id);
    orden.marcarEnProceso();
    return mapToResponseDTO(ordenRepository.save(orden));
  }

  public OrdenResponseDTO marcarEnviada(UUID id, InfoEnvio infoEnvio) {
    Orden orden = buscarPorId(id);
    orden.marcarEnviada(infoEnvio);
    return mapToResponseDTO(ordenRepository.save(orden));
  }

  public OrdenResponseDTO marcarEntregada(UUID id) {
    Orden orden = buscarPorId(id);
    orden.marcarEntregada();
    return mapToResponseDTO(ordenRepository.save(orden));
  }

  public OrdenResponseDTO cancelar(UUID id, String motivo) {
    Orden orden = buscarPorId(id);
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
