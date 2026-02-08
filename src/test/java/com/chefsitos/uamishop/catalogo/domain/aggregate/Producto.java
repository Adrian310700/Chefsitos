package com.chefsitos.uamishop.catalogo.domain.aggregate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;
import com.chefsitos.uamishop.catalogo.domain.valueObject.*;
import com.chefsitos.uamishop.shared.domain.valueObject.Money;;

public class Producto {
  private ProductoId id;
  private String nombre;
  private String descripcion;
  private Money precio;
  private CategoriaId categoriaId;
  private List<Imagen> imagenes;
  private boolean disponible;
  private LocalDateTime fechaCreacion;

  public Producto crear(String nombre, String descripcion, Money precio, CategoriaId categoria) {

    if (this.id != null) {
      throw new IllegalStateException("El producto ya fue creado");
    }

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
    this.id = ProductoId.generar();
    this.nombre = nombre.trim();
    this.descripcion = descripcion.trim();
    this.precio = precio;
    this.categoriaId = categoria;
    this.imagenes = new ArrayList<>();
    // Asumiendo que debe validarse las reglas de negocio antes que un nuevo
    // producto pueda ser activado
    this.disponible = false;
    this.fechaCreacion = LocalDateTime.now();
    return this;
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
    if (nuevoPrecio.valor().compareTo(limite.valor()) > 0) {
      throw new IllegalArgumentException("El precio no puede incrementarse más del 50% en un solo cambio");
    }
    this.precio = nuevoPrecio;
  }

  public void activar() {
    // RN-CAT-09
    if (this.imagenes.size() == 0) {
      throw new IllegalArgumentException(
          "El producto solo puede volver a activarse si tiene al menos una imagen");
    }
    // RN-CAT-10
    if (!(this.precio.esMayorQueCero())) {
      throw new IllegalArgumentException(
          "El producto solo puede volver a activarse si tiene un precio mayor a cero");
    }
    this.disponible = true;
  }

  public void desactivar() {
    // RN-CAT-08
    if (!(this.disponible)) {
      throw new IllegalArgumentException("No se puede volver a desactivar un producto ya desactivado");
    }
    this.disponible = false;
  }

  public void agregarImagen(Imagen imagen) {
    // RN-CAT-06
    if (this.imagenes.size() >= 5) {
      throw new IllegalArgumentException("Un producto no puede tener mas de 5 imagenes");
    }
    // RN-CAT-07
    if (!(imagen.getUrl().startsWith("http://") || imagen.getUrl().startsWith("https://"))) {
      throw new IllegalArgumentException("La URL debe ser valida, empezar con http:// o https://");
    }
    this.imagenes.add(imagen);
  }

  public void removerImagen(ImagenId imagenId) {
    // RN-CAT-13
    if (imagenes.size() <= 1) {
      throw new IllegalArgumentException("El producto debe tener al menos una imagen");
    }

    boolean removido = imagenes.removeIf(img -> img.getId().equals(imagenId));

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

  public boolean getDisponible() {
    return this.disponible;
  }

  public LocalDateTime getFechaCreacion() {
    return this.fechaCreacion;
  }
}
