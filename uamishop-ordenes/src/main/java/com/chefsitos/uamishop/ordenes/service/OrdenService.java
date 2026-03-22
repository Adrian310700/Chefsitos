package com.chefsitos.uamishop.ordenes.service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import java.util.UUID;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.chefsitos.uamishop.shared.util.LogColor.*;

import lombok.extern.slf4j.Slf4j;

import com.chefsitos.uamishop.catalogo.api.CatalogoApi;
import com.chefsitos.uamishop.catalogo.api.dto.ProductoDTO;
import com.chefsitos.uamishop.ordenes.config.RabbitConfig;
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
import com.chefsitos.uamishop.shared.event.OrdenCreadaEvent;
import com.chefsitos.uamishop.shared.event.ProductoCompradoEvent;
import com.chefsitos.uamishop.shared.exception.ResourceNotFoundException;
import com.chefsitos.uamishop.ventas.api.CarritoApi;
import com.chefsitos.uamishop.ventas.api.dto.CarritoDTO;
import com.chefsitos.uamishop.ventas.api.dto.ItemCarritoDTO;

@Slf4j
@Service
public class OrdenService {

  private final OrdenJpaRepository ordenRepository;
  private final CatalogoApi productoService;
  private final CarritoApi carritoService;
  private final ApplicationEventPublisher eventPublisher;
  private final RabbitTemplate rabbitTemplate;

  public OrdenService(OrdenJpaRepository ordenRepository, CatalogoApi productoApi, CarritoApi carritoService,
      ApplicationEventPublisher eventPublisher, RabbitTemplate rabbitTemplate) {
    this.ordenRepository = ordenRepository;
    this.productoService = productoApi;
    this.carritoService = carritoService;
    this.eventPublisher = eventPublisher;
    this.rabbitTemplate = rabbitTemplate;
  }

  public Orden buscarPorId(UUID id) {
    Orden orden = obtenerOrden(id);
    return orden;
  }

  public List<Orden> buscarTodas() {
    return ordenRepository.findAll();
  }

  @Transactional
  public Orden confirmarOrden(UUID id) {
    Orden orden = obtenerOrden(id);
    orden.confirmar();
    return ordenRepository.save(orden);
  }

  @Transactional
  public Orden cancelarOrden(UUID id, String motivo) {
    Orden orden = obtenerOrden(id);
    orden.cancelar(motivo);
    return ordenRepository.save(orden);
  }

  @Transactional
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
          ProductoDTO producto = productoService.buscarPorId(UUID.fromString(i.productoId()));
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

    Orden ordenGuardada = ordenRepository.save(orden);

    List<ProductoCompradoEvent.ItemComprado> itemsEvento = items.stream()
        .map(item -> new ProductoCompradoEvent.ItemComprado(
            item.getProductoId().getValue(),
            item.getSku(),
            item.getCantidad(),
            item.getPrecioUnitario().cantidad(),
            item.getPrecioUnitario().moneda()))
        .toList();

    ProductoCompradoEvent evento = new ProductoCompradoEvent(
        UUID.randomUUID(),
        Instant.now(),
        ordenGuardada.getId().getValue(),
        ordenGuardada.getClienteId().valor(),
        itemsEvento);

    eventPublisher.publishEvent(evento); // Publicar evento interno
    rabbitTemplate.convertAndSend( // Publicar evento via RabbitMQ
        RabbitConfig.EVENTS_EXCHANGE,
        RabbitConfig.RK_PRODUCTO_COMPRADO,
        evento);
    log.info(ROSA + "Evento: ProductoComprado emitido" + RESET
        + " | ordenId={}, clienteId={}, totalItems={}",
        ordenGuardada.getId().getValue(), ordenGuardada.getClienteId().valor(), itemsEvento.size());

    return mapToResponseDTO(ordenGuardada);
  }

  @Transactional
  public OrdenResponseDTO crearDesdeCarrito(CarritoId carritoId, DireccionEnvio direccionEnvio) {
    carritoService.validarCarritoEnCheckout(carritoId.getValue());

    CarritoDTO carrito = carritoService.obtenerCarrito(carritoId.getValue());

    ClienteId clienteOrden = ClienteId.of(carrito.clienteId().toString());

    List<ItemOrden> itemsOrden = carrito.items().stream()
        .map(OrdenService::mapItemCarritoToItemOrden)
        .collect(Collectors.toList());

    ResumenPago resumenPendiente = new ResumenPago(
        "PENDIENTE", null, EstadoPago.PENDIENTE, null);

    Orden nuevaOrden = Orden.crear(clienteOrden, itemsOrden, direccionEnvio, resumenPendiente);

    nuevaOrden = ordenRepository.save(nuevaOrden);

    List<ProductoCompradoEvent.ItemComprado> itemsEvento = itemsOrden.stream()
        .map(item -> new ProductoCompradoEvent.ItemComprado(
            item.getProductoId().getValue(),
            item.getSku(),
            item.getCantidad(),
            item.getPrecioUnitario().cantidad(),
            item.getPrecioUnitario().moneda()))
        .toList();

    ProductoCompradoEvent eventoProductos = new ProductoCompradoEvent(
        UUID.randomUUID(),
        Instant.now(),
        nuevaOrden.getId().getValue(),
        nuevaOrden.getClienteId().valor(),
        itemsEvento);

    eventPublisher.publishEvent(eventoProductos); // Publicar evento interno
    rabbitTemplate.convertAndSend( // Publicar evento via RabbitMQ
        RabbitConfig.EVENTS_EXCHANGE,
        RabbitConfig.RK_PRODUCTO_COMPRADO,
        eventoProductos);
    log.info(ROSA + "Evento: ProductoComprado emitido" + RESET
        + " | ordenId={}, clienteId={}, totalItems={}",
        nuevaOrden.getId().getValue(), nuevaOrden.getClienteId().valor(), itemsEvento.size());

    OrdenCreadaEvent ordenCreadaEvent = new OrdenCreadaEvent(
        UUID.randomUUID(),
        Instant.now(),
        nuevaOrden.getId().getValue(),
        carritoId.getValue(),
        nuevaOrden.getClienteId().valor());

    eventPublisher.publishEvent(ordenCreadaEvent); // Publicar evento interno
    rabbitTemplate.convertAndSend( // Publicar evento via RabbitMQ
        RabbitConfig.EVENTS_EXCHANGE,
        RabbitConfig.RK_ORDEN_CREADA,
        ordenCreadaEvent);
    log.info(ROSA + "Evento: OrdenCreada emitido" + RESET
        + " | ordenId={}, carritoId={}, clienteId={}",
        nuevaOrden.getId().getValue(), carritoId.getValue(), nuevaOrden.getClienteId().valor());

    return mapToResponseDTO(nuevaOrden);
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
        item.sku(),
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
