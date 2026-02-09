package com.chefsitos.uamishop.catalogo.domain.entity;

import com.chefsitos.uamishop.catalogo.domain.valueObject.CategoriaId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;

@DisplayName("Dominio: Entidad - Categoria")
class CategoriaTest {

  private String nombreValido = "Electrónica";
  private String descValida = "Gadgets y dispositivos";
  private CategoriaId categoriaId;

  // Se ejecuta este bloque de codigo antes de cada test
  @BeforeEach
  void setUp() {
    categoriaId = CategoriaId.generar();
  }

  @Test
  @DisplayName("Debe crear categoría con estado inicial correcto")
  void debeCrearCategoria() {
    Categoria cat = Categoria.crear(categoriaId, nombreValido, descValida);

    assertAll("Estado Inicial",
        () -> assertEquals(categoriaId, cat.getCategoriaId()),
        () -> assertEquals(nombreValido, cat.getNombre()),
        () -> assertNull(cat.getCategoriaPadreId(), "Padre debe ser nulo al inicio"));
  }

  @Test
  @DisplayName("Debe permitir actualizar información básica")
  void debeActualizarInfo() {
    Categoria cat = Categoria.crear(categoriaId, nombreValido, descValida);
    cat.actualizar("Hogar", "Cosas de casa");

    assertEquals("Hogar", cat.getNombre());
    assertEquals("Cosas de casa", cat.getDescripcion());
  }

  @Test
  @DisplayName("Debe asignar categoría padre correctamente")
  void debeAsignarPadre() {
    Categoria hijo = Categoria.crear(CategoriaId.generar(), "TVs", "Televisores");
    CategoriaId padreId = CategoriaId.generar();

    hijo.asignarPadre(padreId);

    assertEquals(padreId, hijo.getCategoriaPadreId());
  }

  @Test
  @DisplayName("Debe prevenir asignarse a sí misma como padre (Circularidad)")
  void debePrevenirCircularidad() {
    Categoria cat = Categoria.crear(categoriaId, nombreValido, descValida);
    assertThrows(IllegalArgumentException.class, () -> cat.asignarPadre(categoriaId));
  }
}
