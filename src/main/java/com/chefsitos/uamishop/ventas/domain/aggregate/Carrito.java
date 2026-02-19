package com.chefsitos.uamishop.ventas.domain.aggregate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.chefsitos.uamishop.shared.domain.valueObject.Money;
import com.chefsitos.uamishop.shared.domain.valueObject.ProductoId;
import com.chefsitos.uamishop.ventas.domain.EstadoCarrito;
import com.chefsitos.uamishop.ventas.domain.TipoDescuento;
import com.chefsitos.uamishop.ventas.domain.entity.ItemCarrito;
import com.chefsitos.uamishop.ventas.domain.valueObject.CarritoId;
import com.chefsitos.uamishop.ventas.domain.valueObject.ClienteId;
import com.chefsitos.uamishop.ventas.domain.valueObject.DescuentoAplicado;
import com.chefsitos.uamishop.ventas.domain.valueObject.ItemCarritoId;
import com.chefsitos.uamishop.ventas.domain.valueObject.ProductoRef;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "carritos")
public class Carrito {
  private static final Integer MAX_ITEMS = 20;
  private static final Money MONTO_MINIMO = new Money(BigDecimal.valueOf(50), "MXN");

  @EmbeddedId
  @AttributeOverride(name = "valor", column = @Column(name = "carrito_id"))
  private CarritoId id;

  @Embedded
  @AttributeOverride(name = "valor", column = @Column(name = "cliente_id"))
  private ClienteId clienteId;

  // Lista de descuentos aplicados al carrito (según diagrama de clases)
  @ElementCollection(fetch = FetchType.LAZY)
  @CollectionTable(name = "carrito_descuentos", joinColumns = @JoinColumn(name = "carrito_id"))
  private List<DescuentoAplicado> descuentos = new ArrayList<>();

  @OneToMany(cascade = CascadeType.ALL, // Si se guarda Carrito, se guardan sus ítems
      orphanRemoval = true, // Si se quita un ítem de la lista, se borra de la BD
      fetch = FetchType.LAZY) // Carga los ítems solo cuando se necesiten
  @JoinColumn(name = "carrito_id") // Columna en la tabla items que referencia al carrito
  private List<ItemCarrito> items = new ArrayList<>();

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private EstadoCarrito estado;

  private LocalDateTime fechaCreacion;
  private LocalDateTime fechaActualizacion;

  // Constructor protegido para JPA
  protected Carrito() {
  }

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
    validarEditable(); // Solo se pueden modificar carritos activos
    // Verificar si el producto ya existe en el carrito usando ProductoId
    ItemCarrito item = buscarItem(producto.getProductoId());
    if (item != null) {
      // RN-VEN-04: Si el producto ya existe, solo incrementamos la cantidad
      item.incrementarCantidad(cantidad);
    } else {
      if (items.size() >= MAX_ITEMS) {
        // RN-VEN-03: Un carrito puede tener máximo 20 productos diferentes
        throw new IllegalStateException("Un carrito no puede tener más de " + MAX_ITEMS + " productos");
      }
      // Si el producto no existe, lo agregamos como nuevo ítem
      items.add(new ItemCarrito(
          ItemCarritoId.generar(),
          producto,
          cantidad,
          precio));
    }
    actualizarFecha();
  }

  public void modificarCantidad(ProductoId productoId, Integer nuevaCantidad) {
    // RN-VEN-06: Solo se pueden modificar carritos activos
    validarEditable();
    ItemCarrito item = obtenerItemObligatorio(productoId);
    // RN-VEN-05: Nueva cantidad mayor a 0, si es 0, eliminamos el producto del
    // carrito
    if (nuevaCantidad == 0) {
      eliminarProducto(productoId);
      return; // Fix: evitar ejecutar actualizarCantidad después de eliminar
    }
    item.actualizarCantidad(nuevaCantidad);
    actualizarFecha();
  }

  public void eliminarProducto(ProductoId productoId) {
    // RN-VEN-07: No se pueden eliminar productos de carritos que no estén activos
    validarEditable();
    ItemCarrito item = obtenerItemObligatorio(productoId);
    // RN-VEN-08: Debe existir el producto en el carrito para eliminarlo (validado
    // en obtenerItemObligatorio)
    items.remove(item);
    actualizarFecha();
  }

  public void vaciar() {
    // RN-VEN-09: Solo se pueden vaciar carritos activos
    validarEditable();
    items.clear();
    actualizarFecha();
  }

  public Money calcularSubtotal() {
    return items.stream()
        .map(ItemCarrito::calcularSubtotal)
        .reduce(Money.zero("MXN"), Money::sumar);
  }

  public Money calcularTotal() {
    Money subtotal = calcularSubtotal();
    if (descuentos.isEmpty())
      return subtotal;
    // Aplicar todos los descuentos de la lista
    Money totalDescuento = Money.zero("MXN");
    for (DescuentoAplicado descuento : descuentos) {
      totalDescuento = totalDescuento.sumar(descuento.calcularDescuento(subtotal));
    }
    return subtotal.restar(totalDescuento);
  }

  public void aplicarDescuento(DescuentoAplicado descuento) {
    // Solo se pueden aplicar descuentos a carritos activos
    validarEditable();
    // RN-VEN-15: Solo se puede aplicar un cupón de descuento por carrito
    if (descuento.tipo() == TipoDescuento.CUPON) {
      boolean yaTieneCupon = descuentos.stream()
          .anyMatch(d -> d.tipo() == TipoDescuento.CUPON);
      if (yaTieneCupon) {
        throw new IllegalStateException("Solo se permite un cupón de descuento por carrito");
      }
    }
    // Calcular el monto descontado y almacenar con el monto calculado
    Money subtotal = calcularSubtotal();
    DescuentoAplicado descuentoConMonto = descuento.conMontoCalculado(subtotal);
    this.descuentos.add(descuentoConMonto);
    actualizarFecha();
  }

  public void iniciarCheckout() {
    // RN-VEN-11: El carrito debe estar en estado ACTIVO para iniciar el checkout
    if (estado != EstadoCarrito.ACTIVO) {
      throw new IllegalStateException("El carrito no está activo");
    }
    // RN-VEN-10: El carrito debe tener al menos un producto para iniciar el
    // checkout
    if (items.isEmpty()) {
      throw new IllegalStateException("El carrito debe tener al menos un producto");
    }
    // RN-VEN-12: El monto mínimo de compra para iniciar el checkout es de $50 MXN
    if (calcularSubtotal().cantidad().compareTo(MONTO_MINIMO.cantidad()) < 0) {
      throw new IllegalStateException("Monto mínimo de compra no alcanzado");
    }
    estado = EstadoCarrito.EN_CHECKOUT;
    actualizarFecha();
  }

  public void completarCheckout() {
    // RN-VEN-13: Solo se completa si esta en checkout
    if (estado != EstadoCarrito.EN_CHECKOUT) {
      throw new IllegalStateException("El carrito no está en checkout");
    }
    estado = EstadoCarrito.COMPLETADO;
    actualizarFecha();
  }

  public void abandonar() {
    // RN-VEN-14: Solo se puede abandonar un carrito que esté en checkout
    if (estado != EstadoCarrito.EN_CHECKOUT) {
      throw new IllegalStateException("El carrito no está en checkout");
    }
    estado = EstadoCarrito.ABANDONADO;
    actualizarFecha();
  }

  // Metodos de apoyo para el dominio
  public int obtenerCantidadItems() {
    return items.size();
  }

  private void validarEditable() {
    if (estado != EstadoCarrito.ACTIVO) {
      throw new IllegalStateException("Solo se pueden modificar carritos activos");
    }
  }

  private ItemCarrito buscarItem(ProductoId productoId) {
    return items.stream()
        .filter(i -> i.getProducto().getProductoId().equals(productoId))
        .findFirst()
        .orElse(null);
  }

  private ItemCarrito obtenerItemObligatorio(ProductoId productoId) {
    ItemCarrito item = buscarItem(productoId);
    if (item == null) {
      throw new IllegalStateException("El producto no existe en el carrito");
    }
    return item;
  }

  private void actualizarFecha() {
    this.fechaActualizacion = LocalDateTime.now();
  }

  public LocalDateTime getFechaActualizacion() {
    return fechaActualizacion;
  }

  public ClienteId getClienteId() {
    return clienteId;
  }

  public List<ItemCarrito> getItems() {
    return items;
  }

  public List<DescuentoAplicado> getDescuentos() {
    return descuentos;
  }
}
