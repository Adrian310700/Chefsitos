package com.chefsitos.uamishop.ventas.domain.entity;

import java.math.BigDecimal;

import com.chefsitos.uamishop.shared.domain.valueObject.Money;
import com.chefsitos.uamishop.ventas.domain.valueObject.ItemCarritoId;
import com.chefsitos.uamishop.ventas.domain.valueObject.ProductoRef;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "items_carrito")
public class ItemCarrito {
  // Se define una cantidad máxima de unidades por producto en el
  // carrito para cumplir con la RN-VEN-02
  private static final int MAX_UNIDADES = 10;
  private Integer cantidad;

  @EmbeddedId
  @AttributeOverride(name = "valor", column = @Column(name = "item_carrito_id"))
  private ItemCarritoId id;

  @Embedded
  @AttributeOverrides({
      @AttributeOverride(name = "productoId.valor", column = @Column(name = "producto_id")),
      @AttributeOverride(name = "nombreProducto", column = @Column(name = "nombre_producto")),
      @AttributeOverride(name = "sku", column = @Column(name = "sku"))
  })
  private ProductoRef producto;

  @Embedded
  @AttributeOverrides({
      @AttributeOverride(name = "cantidad", column = @Column(name = "precio_unitario")),
      @AttributeOverride(name = "moneda", column = @Column(name = "precio_unitario_moneda"))
  })
  private Money precioUnitario;

  protected ItemCarrito() {
  }

  public ItemCarrito(ItemCarritoId id, ProductoRef producto, Integer cantidad, Money precioUnitario) {
    // Valida que la cantidad de un producto en el carrito sea positiva
    // RN-VEN-01
    if (cantidad <= 0) {
      throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
    }
    // Valida que la cantidad de un producto en el carrito no exceda las 10 unidades
    // RN-VEN-02
    if (cantidad > MAX_UNIDADES) {
      throw new IllegalArgumentException("La cantidad máxima por producto son 10 unidades");
    }
    this.id = id;
    this.producto = producto;
    this.cantidad = cantidad;
    this.precioUnitario = precioUnitario;
  }

  public void actualizarCantidad(Integer nuevaCantidad) {
    // Valida que la nueva cantidad de un producto en el carrito sea positiva y no
    // exceda las 10 unidades para cumplir con las RN-VEN-01 y RN-VEN-02
    // RN-VEN-05: Cantidad debe ser mayor a 0
    if (nuevaCantidad <= 0) {
      throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
    }
    if (nuevaCantidad > MAX_UNIDADES) {
      throw new IllegalArgumentException("La cantidad máxima por producto son 10 unidades");
    }
    this.cantidad = nuevaCantidad;
  }

  public void incrementarCantidad(Integer cantidad) {
    Integer nuevaCantidad = this.cantidad + cantidad;
    // Valida que la nueva cantidad de un producto en el carrito no exceda las 10
    // unidades para cumplir con la RN-VEN-02
    if (nuevaCantidad > MAX_UNIDADES) {
      throw new IllegalArgumentException("La cantidad máxima por producto son 10 unidades");
    }
    this.cantidad = nuevaCantidad;
  }

  public Money calcularSubtotal() {
    return precioUnitario.multiplicar(BigDecimal.valueOf(cantidad));
  }

  public ProductoRef getProducto() {
    return producto;
  }

  public Integer getCantidad() {
    return cantidad;
  }

  public Money getPrecioUnitario() {
    return precioUnitario;
  }
}
