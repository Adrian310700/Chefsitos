package com.chefsitos.uamishop.ordenes.service;

import com.chefsitos.uamishop.ordenes.controller.dto.OrdenRequest;
import com.chefsitos.uamishop.ordenes.controller.dto.OrdenResponseDTO;
import com.chefsitos.uamishop.ordenes.domain.aggregate.Orden;
import com.chefsitos.uamishop.ordenes.domain.entity.ItemOrden;
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

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityNotFoundException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrdenService {

  private final OrdenJpaRepository ordenRepository;
  private final CarritoService carritoService;
  private final ProductoService productoService;
  private static final String MONEDA_DEFAULT = "MXN";

  public OrdenService(OrdenJpaRepository ordenRepository,
                      CarritoService carritoService,
                      ProductoService productoService) {
    this.ordenRepository = ordenRepository;
    this.carritoService = carritoService;
    this.productoService = productoService;
  }

  @Transactional
  public Orden crearDesdeCarrito(CarritoId carritoId, DireccionEnvio direccionEnvio) {
    Carrito carrito = carritoService.obtenerCarrito(carritoId);
    ClienteId clienteOrden = ClienteId.of(carrito.getClienteId().valor().toString());

    List<ItemOrden> itemsOrden = carrito.getItems().stream()
      .map(this::mapItemCarritoToItemOrden)
      .collect(Collectors.toList());

    Orden nuevaOrden = Orden.crear(clienteOrden, itemsOrden, direccionEnvio);
    nuevaOrden = ordenRepository.save(nuevaOrden);
    carritoService.completarCheckout(carritoId);
    return nuevaOrden;
  }

  @Transactional
  public OrdenResponseDTO crear(OrdenRequest request) {
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
        ProductoId.of(i.productoId()),
        i.nombreProducto(),
        i.sku(),
        i.cantidad(),
        new Money(i.precioUnitario(), MONEDA_DEFAULT)
      ))
      .collect(Collectors.toList());

    Orden orden = Orden.crear(new ClienteId(request.clienteId()), items, direccion);
    return mapToResponseDTO(ordenRepository.save(orden));
  }

  @Transactional(readOnly = true)
  public OrdenResponseDTO buscarPorId(UUID id) {
    Orden orden = ordenRepository.findById(new OrdenId(id))
      .orElseThrow(() -> new EntityNotFoundException("Orden no encontrada con ID: " + id));
    return mapToResponseDTO(orden);
  }

  @Transactional
  public OrdenResponseDTO confirmar(UUID id) {
    Orden orden = buscarEntidadPorId(id);
    orden.confirmar();
    return mapToResponseDTO(ordenRepository.save(orden));
  }

  @Transactional
  public OrdenResponseDTO procesarPago(UUID id, String referenciaPago) {
    Orden orden = buscarEntidadPorId(id);
    orden.procesarPago(referenciaPago);
    return mapToResponseDTO(ordenRepository.save(orden));
  }

  @Transactional
  public OrdenResponseDTO marcarEnviada(UUID id, String numeroGuia, String proveedor) {
    Orden orden = buscarEntidadPorId(id);
    orden.marcarEnviada(numeroGuia, proveedor);
    return mapToResponseDTO(ordenRepository.save(orden));
  }

  @Transactional
  public OrdenResponseDTO cancelar(UUID id, String motivo) {
    Orden orden = buscarEntidadPorId(id);
    orden.cancelar(motivo);
    return mapToResponseDTO(ordenRepository.save(orden));
  }

  @Transactional
  public OrdenResponseDTO marcarEnProceso(UUID id) {
    Orden orden = buscarEntidadPorId(id);
    orden.marcarEnProceso();
    return mapToResponseDTO(ordenRepository.save(orden));
  }

  @Transactional
  public OrdenResponseDTO marcarEntregada(UUID id) {
    Orden orden = buscarEntidadPorId(id);
    orden.marcarEntregada();
    return mapToResponseDTO(ordenRepository.save(orden));
  }

  private Orden buscarEntidadPorId(UUID id) {
    return ordenRepository.findById(new OrdenId(id))
      .orElseThrow(() -> new EntityNotFoundException("Orden no encontrada"));
  }

  private ItemOrden mapItemCarritoToItemOrden(ItemCarrito itemCarrito) {
    ProductoId prodId = ProductoId.of(itemCarrito.getProducto().getProductoId().toString());
    return new ItemOrden(prodId, itemCarrito.getProducto().nombreProducto(),
      itemCarrito.getProducto().sku(), itemCarrito.getCantidad(), itemCarrito.getPrecioUnitario());
  }

  private OrdenResponseDTO mapToResponseDTO(Orden orden) {
    return new OrdenResponseDTO(
      orden.getId().valor(), orden.getNumeroOrden(), orden.getClienteId().valor(),
      orden.getEstado(), orden.getTotal().cantidad(), orden.getTotal().moneda(),
      orden.getDireccionEnvio().calle() + ", " + orden.getDireccionEnvio().ciudad()
    );
  }
}
