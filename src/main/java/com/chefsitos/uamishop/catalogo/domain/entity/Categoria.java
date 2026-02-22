package com.chefsitos.uamishop.catalogo.domain.entity;

import com.chefsitos.uamishop.catalogo.domain.valueObject.CategoriaId;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import com.chefsitos.uamishop.shared.exception.BusinessRuleException;

@Entity
@Table(name = "categorias")
public class Categoria {

  @EmbeddedId
  @AttributeOverride(name = "valor", column = @Column(name = "id")) // Aseguramos nombre 'id'
  private CategoriaId id;

  private String nombre;
  private String descripcion;

  @Embedded
  @AttributeOverride(name = "valor", column = @Column(name = "padre_id", nullable = true))
  private CategoriaId categoriaPadreId;

  private Categoria() {
  }

  public static Categoria crear(CategoriaId id, String nombre, String descripcion) {
    if (id == null) {
      throw new BusinessRuleException("El id de la categoría no puede ser nulo");
    }
    if (nombre == null || nombre.isBlank() || nombre.trim().length() < 3 || nombre.trim().length() > 100) {
      throw new BusinessRuleException("El nombre de la categoría debe tener entre 3 y 100 caracteres");
    }
    if (descripcion == null || descripcion.isBlank() || descripcion.length() > 500) {
      throw new BusinessRuleException("La descripción de la categoría no debe exceder los 500 caracteres");
    }

    Categoria categoria = new Categoria();
    categoria.nombre = nombre.trim();
    categoria.descripcion = descripcion.trim();
    categoria.id = id;
    return categoria;
  }

  public void actualizar(String nombre, String descripcion) {
    // RN-CAT-14
    if (nombre == null || nombre.isBlank() || nombre.trim().length() < 3 || nombre.trim().length() > 100) {
      throw new BusinessRuleException("El nuevo nombre debe tener entre 3 y 100 caracteres");
    }
    // RN-CAT-15
    if (descripcion == null || descripcion.isBlank() || descripcion.length() > 500) {
      throw new BusinessRuleException("La nueva descripción no debe exceder los 500 caracteres");
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
      throw new BusinessRuleException(
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
