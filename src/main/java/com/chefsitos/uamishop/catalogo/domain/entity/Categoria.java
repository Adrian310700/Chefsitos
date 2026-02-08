package com.chefsitos.uamishop.catalogo.domain.entity;

import com.chefsitos.uamishop.catalogo.domain.valueObject.CategoriaId;

public class Categoria {

  private CategoriaId id;
  private String nombre;
  private String descripcion;
  private CategoriaId categoriaPadreId;

  public Categoria(String nombre, String descripcion) {
    this.id = CategoriaId.generar();
    actualizar(nombre, descripcion);
  }

  public void actualizar(String nombre, String descripcion) {
    // RN-CAT-14
    if (nombre == null || nombre.isBlank() || nombre.trim().length() < 3 || nombre.trim().length() > 100) {
      throw new IllegalArgumentException("El nuevo nombre debe tener entre 3 y 100 caracteres");
    }
    // RN-CAT-15
    if (descripcion == null || descripcion.isBlank() || descripcion.length() > 500) {
      throw new IllegalArgumentException("La nueva descripción no debe exceder los 500 caracteres");
    }
    this.nombre = nombre.trim();
    this.descripcion = descripcion.trim();
  }

  public void asignarPadre(CategoriaId categoriaPadreId) {
    if (categoriaPadreId == null) {
      this.categoriaPadreId = null;
      return;
    }
    // RN-CAT-16
    if (this.id.equals(categoriaPadreId)) {
      throw new IllegalArgumentException(
          "La id de la categoria padre no puede ser la misma que la de la categoria actual");
    }
    this.categoriaPadreId = categoriaPadreId;
  }

  public CategoriaId getCategoriaId() {
    return this.id;
  }

  public String getNombre() {
    return this.nombre;
  }

  public String getDescripcion() {
    return this.descripcion;
  }

  public CategoriaId getCategoriaPadreId() {
    return this.categoriaPadreId;
  }
}
