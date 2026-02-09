package com.chefsitos.uamishop.ventas.domain.aggregate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.chefsitos.uamishop.ventas.domain.EstadoCarrito;
import com.chefsitos.uamishop.ventas.domain.entity.ItemCarrito;
import com.chefsitos.uamishop.ventas.domain.valueObject.CarritoId;
import com.chefsitos.uamishop.ventas.domain.valueObject.ClienteId;
import com.chefsitos.uamishop.ventas.domain.valueObject.DescuentoAplicado;
import com.chefsitos.uamishop.ventas.domain.valueObject.ItemCarritoId;
import com.chefsitos.uamishop.ventas.domain.valueObject.ProductoRef;

import com.chefsitos.uamishop.shared.domain.valueObject.Money;

public class Carrito {
  private static final int MAX_ITEMS = 20;
  private static final Money MONTO_MINIMO = new Money(BigDecimal.valueOf(50), "MXN");

  private final CarritoId id;
  private final ClienteId clienteId;
  private final List<ItemCarrito> items = new ArrayList<>();
  // Se cambio la lista de descuentos por un solo descuento aplicado, para
  // validar RN-VEN-15
  private DescuentoAplicado descuento;
  private EstadoCarrito estado;
  private LocalDateTime fechaCreacion;
  private LocalDateTime fechaActualizacion;

  private Carrito(CarritoId id, ClienteId clienteId) {
    this.id = id;
    this.clienteId = clienteId;
    this.estado = EstadoCarrito.ACTIVO;
    this.fechaCreacion = LocalDateTime.now();
    this.fechaActualizacion = this.fechaCreacion;
  }

  public static Carrito crear(ClienteId clienteId) {
    return new Carrito(CarritoId.generar(), clienteId);
  }

  public void agregarProducto(ProductoRef producto, int cantidad, Money precio) {
    validarEditable();
    ItemCarrito item = buscarItem(producto.getProductoId());
    if (item != null) {
      item.incrementarCantidad(cantidad);
    } else {
      if (items.size() >= MAX_ITEMS) {
        throw new IllegalStateException("Máximo de productos distintos alcanzado");
      }
      items.add(new ItemCarrito(
          ItemCarritoId.generar(),
          producto,
          cantidad,
          precio));
    }
    ActualizarFecha();
  }

  public void modificarCantidad(UUID productoId, int nuevaCantidad) {
    validarEditable();
    ItemCarrito item = obtenerItemObligatorio(productoId);
    item.actualizarCantidad(nuevaCantidad);
    ActualizarFecha();
  }

  public void eliminarProducto(UUID productoId) {
    validarEditable();
    ItemCarrito item = obtenerItemObligatorio(productoId);
    items.remove(item);
    ActualizarFecha();
  }

  public void vaciar() {
    validarEditable();
    items.clear();
    ActualizarFecha();
  }

  public Money calcularSubtotal() {
    return items.stream()
        .map(ItemCarrito::calcularSubtotal)
        .reduce(Money.zero("MXN"), Money::sumar);
  }

  public Money calcularTotal() {
    Money subtotal = calcularSubtotal();
    if (descuento == null)
      return subtotal;
    return subtotal.restar(descuento.calcularDescuento(subtotal));
  }

  public void aplicarDescuento(DescuentoAplicado descuento) {
    if (this.descuento != null) {
      throw new IllegalStateException("Solo se permite un descuento por carrito");
    }
    this.descuento = descuento;
    ActualizarFecha();
  }

  public void iniciarCheckout() {
    if (estado != EstadoCarrito.ACTIVO) {
      throw new IllegalStateException("El carrito no está activo");
    }
    if (items.isEmpty()) {
      throw new IllegalStateException("El carrito debe tener al menos un producto");
    }
    if (calcularSubtotal().valor().compareTo(MONTO_MINIMO.valor()) < 0) {
      throw new IllegalStateException("Monto mínimo de compra no alcanzado");
    }
    estado = EstadoCarrito.EN_CHECKOUT;
    ActualizarFecha();
  }

  public void completarCheckout() {
    if (estado != EstadoCarrito.EN_CHECKOUT) {
      throw new IllegalStateException("El carrito no está en checkout");
    }
    estado = EstadoCarrito.COMPLETADO;
    ActualizarFecha();
  }

  public void abandonar() {
    if (estado != EstadoCarrito.EN_CHECKOUT) {
      throw new IllegalStateException("El carrito no está en checkout");
    }
    estado = EstadoCarrito.ABANDONADO;
    ActualizarFecha();
  }

  public int obtenerCantidadItems() {
    return items.size();
  }

  private void validarEditable() {
    if (estado != EstadoCarrito.ACTIVO) {
      throw new IllegalStateException("Solo se pueden modificar carritos activos");
    }
  }

  private ItemCarrito buscarItem(UUID productoId) {
    return items.stream()
        .filter(i -> i.getProducto().getProductoId().equals(productoId))
        .findFirst()
        .orElse(null);
  }

  private ItemCarrito obtenerItemObligatorio(UUID productoId) {
    ItemCarrito item = buscarItem(productoId);
    if (item == null) {
      throw new IllegalStateException("El producto no existe en el carrito");
    }
    return item;
  }

  private void ActualizarFecha() {
    this.fechaActualizacion = LocalDateTime.now();
  }
}
