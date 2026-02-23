package com.chefsitos.uamishop.ordenes.service;

import com.chefsitos.uamishop.ordenes.controller.dto.OrdenRequest;
import com.chefsitos.uamishop.ordenes.controller.dto.OrdenResponseDTO;
import com.chefsitos.uamishop.ordenes.domain.aggregate.Orden;
import com.chefsitos.uamishop.ordenes.domain.entity.ItemOrden;
import com.chefsitos.uamishop.ordenes.domain.enumeration.EstadoPago;
import com.chefsitos.uamishop.ordenes.domain.valueObject.*;
import com.chefsitos.uamishop.ordenes.repository.OrdenJpaRepository;
import com.chefsitos.uamishop.shared.domain.valueObject.ClienteId;
import com.chefsitos.uamishop.shared.domain.valueObject.Money;
import com.chefsitos.uamishop.shared.domain.valueObject.ProductoId;
import com.chefsitos.uamishop.ventas.domain.aggregate.Carrito;
import com.chefsitos.uamishop.ventas.domain.entity.ItemCarrito;
import com.chefsitos.uamishop.ventas.domain.valueObject.CarritoId;
import com.chefsitos.uamishop.ventas.service.CarritoService;
import com.chefsitos.uamishop.catalogo.service.ProductoService;
import com.chefsitos.uamishop.catalogo.controller.dto.ProductoResponse;
import com.chefsitos.uamishop.shared.exception.ResourceNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrdenService {

  @Autowired
  private OrdenJpaRepository ordenRepository;

  @Autowired
  private CarritoService carritoService;

  @Autowired
  private ProductoService productoService;

  private static final String MONEDA_DEFAULT = "MXN";

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
          ProductoResponse producto = productoService.buscarPorId(UUID.fromString(i.productoId()));
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
    Carrito carrito = carritoService.obtenerCarrito(carritoId);
    ClienteId clienteOrden = ClienteId.of(carrito.getClienteId().valor().toString());

    List<ItemOrden> itemsOrden = carrito.getItems().stream()
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
  public static ItemOrden mapItemCarritoToItemOrden(ItemCarrito itemCarrito) {
    ProductoId prodId = ProductoId.of(itemCarrito.getProducto().getProductoId().valor().toString());
    return new ItemOrden(
        prodId,
        itemCarrito.getProducto().nombreProducto(),
        itemCarrito.getProducto().sku(),
        itemCarrito.getCantidad(),
        itemCarrito.getPrecioUnitario());
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
