package com.chefsitos.uamishop.ordenes.domain.entity;

import com.chefsitos.uamishop.ordenes.domain.valueObject.ItemOrdenId;
import com.chefsitos.uamishop.shared.domain.valueObject.Money;
import com.chefsitos.uamishop.shared.domain.valueObject.ProductoId;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "orden_items")
public class ItemOrden {

  @EmbeddedId
  @AttributeOverride(name = "valor", column = @Column(name = "id"))
  private ItemOrdenId id;
  private ProductoId productoId;
  private String nombreProducto;
  private String sku;
  private Integer cantidad;

  @Embedded
  @AttributeOverrides({
      @AttributeOverride(name = "valor", column = @Column(name = "precio_unitario_monto")),
      @AttributeOverride(name = "moneda", column = @Column(name = "precio_unitario_moneda"))
  })
  private Money precioUnitario;

  @Embedded
  @AttributeOverrides({
      @AttributeOverride(name = "valor", column = @Column(name = "subtotal_monto")),
      @AttributeOverride(name = "moneda", column = @Column(name = "subtotal_moneda"))
  })
  private Money subtotal;

  // Constructor de dominio
  public ItemOrden(ProductoId productoId, String nombreProducto, String sku, int cantidad, Money precioUnitario) {
    if (cantidad <= 0) {
      throw new IllegalArgumentException("La cantidad del item debe ser mayor a cero");
    }
    this.id = ItemOrdenId.generar();
    this.productoId = productoId;
    this.nombreProducto = nombreProducto;
    this.sku = sku;
    this.cantidad = cantidad;
    this.precioUnitario = precioUnitario;
    this.subtotal = precioUnitario.multiplicar(new BigDecimal(cantidad));
  }

  public Money calcularSubtotal() {
    return precioUnitario.multiplicar(new BigDecimal(cantidad));
  }

  // Getters necesarios para la persistencia y lectura
  public ItemOrdenId getId() {
    return id;
  }

  public ProductoId getProductoId() {
    return productoId;
  }

  public String getNombreProducto() {
    return nombreProducto;
  }

  public int getCantidad() {
    return cantidad;
  }

  public Money getPrecioUnitario() {
    return precioUnitario;
  }

  public String getSku() {
    return sku;
  }
}
