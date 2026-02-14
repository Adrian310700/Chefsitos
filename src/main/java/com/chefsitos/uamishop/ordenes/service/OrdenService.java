package com.chefsitos.uamishop.ordenes.service;

import com.chefsitos.uamishop.ordenes.controller.dto.OrdenRequest;
import com.chefsitos.uamishop.ordenes.domain.aggregate.Orden;
import com.chefsitos.uamishop.ordenes.domain.entity.ItemOrden;
import com.chefsitos.uamishop.ordenes.domain.valueObject.*;
import com.chefsitos.uamishop.ordenes.repository.OrdenJpaRepository;
import com.chefsitos.uamishop.ventas.domain.aggregate.Carrito;
import com.chefsitos.uamishop.ventas.domain.entity.ItemCarrito;
import com.chefsitos.uamishop.ventas.domain.valueObject.CarritoId;
import com.chefsitos.uamishop.ventas.domain.valueObject.ProductoRef;
import com.chefsitos.uamishop.ventas.service.CarritoService;
import com.chefsitos.uamishop.shared.domain.valueObject.Money;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Servicio de aplicación para el Bounded Context "Órdenes".
 */
@Service
@Transactional
public class OrdenService {

  private final OrdenJpaRepository ordenRepository;
  private final CarritoService carritoService;

  public OrdenService(OrdenJpaRepository ordenRepository, CarritoService carritoService) {
    this.ordenRepository = ordenRepository;
    this.carritoService = carritoService;
  }

  /**
   * Crea una nueva orden a partir del request.
   * Construye DireccionEnvio e ItemOrden desde el request.
   * Crea la orden.
   * Persiste y devuelve el agregado Orden.
   */
  public Orden crear(OrdenRequest request) {
    if (request == null) {
      throw new IllegalArgumentException("OrdenRequest no puede ser null");
    }
    if (request.items() == null || request.items().isEmpty()) {
      throw new IllegalArgumentException("La orden debe tener al menos un item");
    }
    if (request.numeroOrden() == null || request.numeroOrden().isBlank()) {
      throw new IllegalArgumentException("numeroOrden es obligatorio");
    }
    if (request.clienteId() == null) {
      throw new IllegalArgumentException("clienteId es obligatorio");
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
            i.precioUnitario()))
        .collect(Collectors.toList());

    Orden orden = new Orden(
        OrdenId.generar(),
        request.numeroOrden(),
        new ClienteId(request.clienteId()),
        items,
        direccion);

    return ordenRepository.save(orden);
  }

  /**
   * Crea una orden desde un carrito
   *
   * pedido:
   * 1) Obtiene el carrito vía CarritoService
   * 2) Convierte items del carrito a ItemOrden
   * 3) Crea la orden, persiste
   * 4) Llama completarCheckout del carrito
   *
   * Nota: Carrito y ItemCarrito no exponen getters para
   * items/clienteId/cantidad/precioUnitario.
   * Para NO tocar esas clases, se usa reflexión.
   */
  public Orden crearDesdeCarrito(CarritoId carritoId, DireccionEnvio direccionEnvio) {
    if (carritoId == null) {
      throw new IllegalArgumentException("carritoId es obligatorio");
    }
    if (direccionEnvio == null) {
      throw new IllegalArgumentException("direccionEnvio es obligatoria");
    }

    // 1) Obtener carrito
    Carrito carrito = carritoService.obtenerCarrito(carritoId);
    if (carrito == null) {
      throw new IllegalArgumentException("No existe el carrito: " + carritoId);
    }

    // 2) Sacar clienteId e items
    com.chefsitos.uamishop.ventas.domain.valueObject.ClienteId clienteIdVentas = readField(carrito, "clienteId",
        com.chefsitos.uamishop.ventas.domain.valueObject.ClienteId.class);

    @SuppressWarnings("unchecked")
    List<ItemCarrito> itemsCarrito = (List<ItemCarrito>) readField(carrito, "items", List.class);

    if (clienteIdVentas == null) {
      throw new IllegalStateException("El carrito no tiene clienteId (no se puede crear la orden)");
    }
    if (itemsCarrito == null || itemsCarrito.isEmpty()) {
      throw new IllegalStateException("El carrito no tiene items (no se puede crear la orden)");
    }

    // 3) Convertir items de carrito -> items de orden
    List<ItemOrden> itemsOrden = itemsCarrito.stream()
        .map(this::mapItemCarritoAItemOrdenSinGetters)
        .collect(Collectors.toList());

    // 4) Crear Orden
    String numeroOrden = "ORD-" + carritoId.getValue().toString().substring(0, 8).toUpperCase();
    ClienteId clienteIdOrden = new ClienteId(clienteIdVentas.getValue());

    Orden orden = new Orden(
        OrdenId.generar(),
        numeroOrden,
        clienteIdOrden,
        itemsOrden,
        direccionEnvio);

    // 5) Persistir
    Orden guardada = ordenRepository.save(orden);

    // 6) Completar checkout
    carritoService.completarCheckout(carritoId);

    return guardada;
  }

  private ItemOrden mapItemCarritoAItemOrdenSinGetters(ItemCarrito itemCarrito) {
    if (itemCarrito == null) {
      throw new IllegalArgumentException("ItemCarrito no puede ser null");
    }

    ProductoRef producto = itemCarrito.getProducto();
    if (producto == null) {
      throw new IllegalStateException("ItemCarrito sin producto (ProductoRef)");
    }

    Integer cantidad = readField(itemCarrito, "cantidad", Integer.class);
    Money precioUnitario = readField(itemCarrito, "precioUnitario", Money.class);

    if (cantidad == null || cantidad <= 0) {
      throw new IllegalStateException("Cantidad inválida en ItemCarrito");
    }
    if (precioUnitario == null) {
      throw new IllegalStateException("precioUnitario es null en ItemCarrito");
    }

    return new ItemOrden(
        ItemOrdenId.generar(),
        producto.productoId().toString(), // en Orden lo manejas como String
        producto.nombreProducto(),
        producto.sku(),
        cantidad,
        precioUnitario);
  }

  /**
   * Lee un campo privado por reflexión SIN modificar la clase.
   */
  @SuppressWarnings("unchecked")
  private static <T> T readField(Object target, String fieldName, Class<T> type) {
    try {
      Field f = target.getClass().getDeclaredField(fieldName);
      f.setAccessible(true);
      Object value = f.get(target);
      if (value == null)
        return null;
      return (T) value;
    } catch (NoSuchFieldException e) {
      throw new IllegalStateException("No existe el campo '" + fieldName + "' en " + target.getClass().getName(), e);
    } catch (IllegalAccessException e) {
      throw new IllegalStateException("No se pudo acceder al campo '" + fieldName + "'", e);
    }
  }

  @Transactional(readOnly = true)
  public Orden buscarPorId(UUID id) {
    if (id == null) {
      throw new IllegalArgumentException("El id no puede ser null");
    }
    return ordenRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Orden no encontrada: " + id));
  }

  @Transactional(readOnly = true)
  public List<Orden> buscarTodas() {
    return ordenRepository.findAll();
  }

  public Orden confirmar(UUID id) {
    Orden orden = buscarPorId(id);
    orden.confirmar();
    return ordenRepository.save(orden);
  }

  public Orden procesarPago(UUID id, String referenciaPago) {
    if (referenciaPago == null || referenciaPago.isBlank()) {
      throw new IllegalArgumentException("La referenciaPago es obligatoria");
    }
    Orden orden = buscarPorId(id);
    orden.procesarPago(referenciaPago);
    return ordenRepository.save(orden);
  }

  public Orden marcarEnProceso(UUID id) {
    Orden orden = buscarPorId(id);
    orden.marcarEnProceso();
    return ordenRepository.save(orden);
  }

  public Orden marcarEnviada(UUID id, InfoEnvio infoEnvio) {
    if (infoEnvio == null) {
      throw new IllegalArgumentException("infoEnvio es obligatorio");
    }
    Orden orden = buscarPorId(id);
    orden.marcarEnviada(infoEnvio.numeroGuia());
    return ordenRepository.save(orden);
  }

  public Orden marcarEntregada(UUID id) {
    Orden orden = buscarPorId(id);
    orden.marcarEntregada();
    return ordenRepository.save(orden);
  }

  public Orden cancelar(UUID id, String motivo) {
    if (motivo == null || motivo.isBlank()) {
      throw new IllegalArgumentException("El motivo es obligatorio");
    }
    Orden orden = buscarPorId(id);
    orden.cancelar(motivo);
    return ordenRepository.save(orden);
  }
}
