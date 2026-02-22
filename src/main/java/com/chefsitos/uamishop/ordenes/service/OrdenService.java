package com.chefsitos.uamishop.ordenes.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chefsitos.uamishop.catalogo.service.ProductoService;
import com.chefsitos.uamishop.ordenes.controller.dto.OrdenRequest;
import com.chefsitos.uamishop.ordenes.domain.aggregate.Orden;
import com.chefsitos.uamishop.ordenes.domain.entity.ItemOrden;
import com.chefsitos.uamishop.ordenes.domain.valueObject.DireccionEnvio;
import com.chefsitos.uamishop.ordenes.domain.valueObject.InfoEnvio;
import com.chefsitos.uamishop.ordenes.domain.valueObject.OrdenId;
import com.chefsitos.uamishop.ordenes.repository.OrdenJpaRepository;
import com.chefsitos.uamishop.shared.domain.valueObject.ClienteId;
import com.chefsitos.uamishop.shared.domain.valueObject.ProductoId;
import com.chefsitos.uamishop.ventas.domain.aggregate.Carrito;
import com.chefsitos.uamishop.ventas.domain.entity.ItemCarrito;
import com.chefsitos.uamishop.ventas.domain.valueObject.CarritoId;
import com.chefsitos.uamishop.ventas.service.CarritoService;

import jakarta.persistence.EntityNotFoundException;

@Service
public class OrdenService {

  @Autowired
  private OrdenJpaRepository ordenRepository;

  @Autowired
  private CarritoService carritoService;

  @Autowired
  ProductoService productoService;

  /**
   * Crea una orden directamente desde un Request
   */
  @Transactional
  public Orden crear(OrdenRequest request) {
    ClienteId clienteId = ClienteId.of(request.clienteId().toString());

    DireccionEnvio direccionEnvio = new DireccionEnvio(
        request.nombreDestinatario(),
        request.calle(),
        request.ciudad(),
        request.estado(),
        request.codigoPostal(),
        request.pais(),
        request.telefono(),
        request.instrucciones());

    // 2. Mapear Items del Request a Entidades de Dominio ItemOrden
    List<ItemOrden> items = request.items().stream()
        .map(itemReq -> new ItemOrden(
            ProductoId.of(itemReq.productoId()),
            itemReq.nombreProducto(),
            itemReq.sku(),
            itemReq.cantidad(),
            itemReq.precioUnitario()))
        .collect(Collectors.toList());

    Orden nuevaOrden = Orden.crear(clienteId, items, direccionEnvio);

    return ordenRepository.save(nuevaOrden);
  }

  /**
   * Crea una orden basada en un Carrito de Compras existente
   */
  @Transactional
  public Orden crearDesdeCarrito(CarritoId carritoId, DireccionEnvio direccionEnvio) {

    // Obtener el carrito desde el servicio de Ventas
    Carrito carrito = carritoService.obtenerCarrito(carritoId);

    ClienteId clienteOrden = ClienteId.of(carrito.getClienteId().valor().toString());

    // Mapeo de Items (ItemCarrito -> ItemOrden)
    List<ItemOrden> itemsOrden = carrito.getItems().stream()
        .map(this::mapItemCarritoToItemOrden)
        .collect(Collectors.toList());

    Orden nuevaOrden = Orden.crear(clienteOrden, itemsOrden, direccionEnvio);

    nuevaOrden = ordenRepository.save(nuevaOrden);

    carritoService.completarCheckout(carritoId);

    return nuevaOrden;
  }

  @Transactional(readOnly = true)
  public Orden buscarPorId(UUID id) {
    return ordenRepository.findById(new OrdenId(id))
        .orElseThrow(() -> new EntityNotFoundException("Orden no encontrada con ID: " + id));
  }

  @Transactional(readOnly = true)
  public List<Orden> buscarTodas() {
    return ordenRepository.findAll();
  }

  // Métodos de transición de estado

  @Transactional
  public Orden confirmar(UUID id) {
    Orden orden = buscarPorId(id);
    orden.confirmar(); // Ejecuta lógica de dominio
    return ordenRepository.save(orden);
  }

  @Transactional
  public Orden procesarPago(UUID id, String referenciaPago) {
    Orden orden = buscarPorId(id);
    orden.procesarPago(referenciaPago); // Ejecuta lógica y actualiza ResumenPago
    return ordenRepository.save(orden);
  }

  @Transactional
  public Orden marcarEnProceso(UUID id) {
    Orden orden = buscarPorId(id);
    orden.marcarEnProceso();
    return ordenRepository.save(orden);
  }

  @Transactional
  public Orden marcarEnviada(UUID id, InfoEnvio infoEnvio) {
    Orden orden = buscarPorId(id);

    orden.marcarEnviada(infoEnvio.numeroGuia(), infoEnvio.proveedorLogistico());

    return ordenRepository.save(orden);
  }

  @Transactional
  public Orden marcarEnTransito(UUID id) {
    Orden orden = buscarPorId(id);
    orden.marcarEnTransito();
    return ordenRepository.save(orden);
  }

  @Transactional
  public Orden marcarEntregada(UUID id) {
    Orden orden = buscarPorId(id);
    orden.marcarEntregada();
    return ordenRepository.save(orden);
  }

  @Transactional
  public Orden cancelar(UUID id, String motivo) {
    Orden orden = buscarPorId(id);
    // Asumimos un usuario de sistema o extraído del contexto de seguridad
    String usuarioResponsable = "ADMIN_O_CLIENTE";

    orden.cancelar(motivo, usuarioResponsable);
    return ordenRepository.save(orden);
  }

  // HELPERS DE MAPEO (ACL - Anti Corruption Layer interna)

  private ItemOrden mapItemCarritoToItemOrden(ItemCarrito itemCarrito) {
    // Convertir ProductoRef (Ventas) -> ProductoId (Shared/Ordenes)
    ProductoId prodId = ProductoId.of(itemCarrito.getProducto().getProductoId().toString());

    return new ItemOrden(
        prodId,
        itemCarrito.getProducto().nombreProducto(),
        itemCarrito.getProducto().sku(),
        itemCarrito.getCantidad(),
        itemCarrito.getPrecioUnitario() // Money es compartido en Shared, pasa directo
    );
  }
}
