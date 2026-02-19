package com.chefsitos.uamishop.catalogo.domain.aggregate;

import com.chefsitos.uamishop.catalogo.domain.valueObject.*;
import com.chefsitos.uamishop.shared.domain.valueObject.Money;
import com.chefsitos.uamishop.shared.domain.valueObject.ProductoId;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "productos")
public class Producto {
  @EmbeddedId
  @AttributeOverride(name = "valor", column = @Column(name = "id")) // Cambia 'valor' por 'id'
  private ProductoId id;

  private String nombre;
  private String descripcion;

  @Embedded
  @AttributeOverrides({
      @AttributeOverride(name = "cantidad", column = @Column(name = "precio_monto")),
      @AttributeOverride(name = "moneda", column = @Column(name = "precio_moneda"))
  })
  private Money precio;

  @Embedded
  @AttributeOverride(name = "valor", column = @Column(name = "categoria_id"))
  private CategoriaId categoriaId;

  @ElementCollection
  @CollectionTable(name = "producto_imagenes", joinColumns = @JoinColumn(name = "producto_id"))
  private List<Imagen> imagenes;
  private boolean disponible;
  private LocalDateTime fechaCreacion;

  // Constructor privado para forzar el uso del metodo de crear
  private Producto() {
  }

  // Factory method para crear un nuevo producto con validaciones de negocio
  public static Producto crear(String nombre, String descripcion, Money precio, CategoriaId categoria) {

    // RN-CAT-01
    if (nombre == null || nombre.trim().length() < 3 || nombre.trim().length() > 100) {
      throw new IllegalArgumentException("El nombre debe tener entre 3 y 100 caracteres");
    }
    // RN-CAT-02
    if (!(precio.esMayorQueCero())) {
      throw new IllegalArgumentException("El precio debe ser mayor a 0");
    }
    // RN-CAT-03
    if (descripcion == null || descripcion.trim().length() > 500) {
      throw new IllegalArgumentException("La descripción no debe exceder los 500 caracteres");
    }
    Producto producto = new Producto();
    producto.id = ProductoId.generar();
    producto.nombre = nombre.trim();
    producto.descripcion = descripcion.trim();
    producto.precio = precio;
    producto.categoriaId = categoria;
    producto.imagenes = new ArrayList<>();
    // El producto esta disponible si tiene al menos una imagen
    producto.disponible = false;
    producto.fechaCreacion = LocalDateTime.now();
    return producto;
  }

  public void actualizarInformacion(String nombre, String descripcion) {
    // RN-CAT-11
    if (nombre == null || nombre.length() < 3 || nombre.length() > 100) {
      throw new IllegalArgumentException("El nuevo nombre debe tener entre 3 y 100 caracteres");
    }
    // RN-CAT-12
    if (descripcion == null || descripcion.length() > 500) {
      throw new IllegalArgumentException("La nueva descripción no debe exceder los 500 caracteres");
    }
    this.nombre = nombre.trim();
    this.descripcion = descripcion.trim();
  }

  public void cambiarPrecio(Money nuevoPrecio) {
    // RN-CAT-04
    if (!nuevoPrecio.esMayorQueCero()) {
      throw new IllegalArgumentException("El nuevo precio no puede ser negativo");
    }
    // RN-CAT-05
    Money limite = precio.sumar(precio.multiplicar(new BigDecimal("0.5")));
    if (nuevoPrecio.cantidad().compareTo(limite.cantidad()) > 0) {
      throw new IllegalArgumentException("El precio no puede incrementarse más del 50% en un solo cambio");
    }
    this.precio = nuevoPrecio;
  }

  public void activar() {

    if (this.disponible) {
      throw new IllegalStateException("El producto ya esta activo");
    }
    // RN-CAT-09
    if (this.imagenes.isEmpty()) {
      throw new IllegalStateException(
          "El producto solo puede volver a activarse si tiene al menos una imagen");
    }
    // RN-CAT-10
    if (!(this.precio.esMayorQueCero())) {
      throw new IllegalStateException(
          "El producto solo puede volver a activarse si tiene un precio mayor a cero");
    }
    this.disponible = true;
  }

  public void desactivar() {
    // RN-CAT-08
    if (!(this.disponible)) {
      throw new IllegalStateException("No se puede volver a desactivar un producto ya desactivado");
    }
    this.disponible = false;
  }

  public void agregarImagen(Imagen imagen) {
    // RN-CAT-06
    if (this.imagenes.size() >= 5) {
      throw new IllegalStateException("Un producto no puede tener mas de 5 imagenes");
    }
    // RN-CAT-07
    this.imagenes.add(imagen);
  }

  public void removerImagen(ImagenId imagenId) {
    // RN-CAT-13: El producto debe tener al menos una imagen
    if (imagenes.size() <= 1) {
      throw new IllegalStateException(
          "El producto debe tener al menos una imagen. No se puede remover la última.");
    }

    boolean removido = imagenes.removeIf(img -> img.id().equals(imagenId));

    if (!removido) {
      throw new IllegalArgumentException("La imagen no existe en el producto");
    }
  }

  public ProductoId getProductoId() {
    return this.id;
  }

  public String getNombre() {
    return this.nombre;
  }

  public String getDescripcion() {
    return this.descripcion;
  }

  public Money getPrecio() {
    return this.precio;
  }

  public CategoriaId getCategoriaId() {
    return this.categoriaId;
  }

  public List<Imagen> getImagenes() {
    return this.imagenes;
  }

  public boolean isDisponible() {
    return this.disponible;
  }

  public LocalDateTime getFechaCreacion() {
    return this.fechaCreacion;
  }
}
