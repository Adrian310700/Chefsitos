package com.chefsitos.uamishop.catalogo.domain.aggregate;

import com.chefsitos.uamishop.catalogo.domain.valueObject.*;
import com.chefsitos.uamishop.shared.domain.valueObject.*;
import org.junit.jupiter.api.*;
import java.math.BigDecimal;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Dominio: Aggregate Root - Producto")
class ProductoTest {

  private CategoriaId categoriaDefault;
  private Money precioDefault;

  @BeforeEach
  void setUp() {
    categoriaDefault = CategoriaId.generar();
    precioDefault = new Money(BigDecimal.valueOf(100.00), "MXN");
  }

  @Nested
  @DisplayName("Reglas de Creación")
  class ReglasCreacion {

    @Test
    @DisplayName("Fallar si nombre < 3 caracteres")
    void falloNombreCorto() {
      assertThrows(IllegalArgumentException.class, () -> Producto.crear("TV", "Desc", precioDefault, categoriaDefault));
    }

    @Test
    @DisplayName("Fallar si nombre > 100 caracteres")
    void falloNombreLargo() {
      String nombreLargo = "A".repeat(101);
      assertThrows(IllegalArgumentException.class,
          () -> Producto.crear(nombreLargo, "Desc", precioDefault, categoriaDefault));
    }

    @Test
    @DisplayName("Fallar si precio <= 0")
    void falloPrecioCero() {
      Money precioCero = new Money(BigDecimal.ZERO, "MXN");
      assertThrows(IllegalArgumentException.class,
          () -> Producto.crear("Producto Valido", "Desc", precioCero, categoriaDefault));
    }

    @Test
    @DisplayName("Fallar si el precio incrementa más del 50%")
    void falloPrecioExcesivo() {
      Producto p = Producto.crear("Producto", "Desc", precioDefault, categoriaDefault);
      Money nuevoPrecio = new Money(BigDecimal.valueOf(151.00), "MXN");
      assertThrows(IllegalArgumentException.class, () -> p.cambiarPrecio(nuevoPrecio),
          "Debe bloquear inflación > 50%");
    }

    @Test
    @DisplayName("Fallar si descripción > 500 caracteres")
    void falloDescripcionLarga() {
      String descLarga = "A".repeat(501);
      assertThrows(IllegalArgumentException.class,
          () -> Producto.crear("Producto", descLarga, precioDefault, categoriaDefault));
    }

    @Test
    @DisplayName("Debe crear producto en estado NO disponible por defecto")
    void crearCorrectamente() {
      Producto p = Producto.crear("Laptop", "Potente", precioDefault, categoriaDefault);

      assertAll("Estado Post-Creación",
          () -> assertNotNull(p.getProductoId()),
          () -> assertFalse(p.isDisponible(), "Debe aparecer desactivado"),
          () -> assertEquals("Laptop", p.getNombre()),
          () -> assertTrue(p.getImagenes().isEmpty()));
    }
  }

  @Nested
  @DisplayName("Lógica de Precios")
  class LogicaPrecios {
    private Producto producto;

    @BeforeEach
    void init() {
      producto = Producto.crear("Base", "Desc", precioDefault, categoriaDefault);
    }

    @Test
    @DisplayName("El nuevo precio no puede ser negativo")
    void falloPrecioNegativo() {
      Money negativo = new Money(BigDecimal.valueOf(-10), "MXN");
      assertThrows(IllegalArgumentException.class, () -> producto.cambiarPrecio(negativo));
    }

    @Test
    @DisplayName("El precio no puede subir más del 50% de golpe")
    void falloIncrementoExcesivo() {
      // Precio actual 100. Límite aumento 50. Nuevo máx 150.
      // Intentamos 151.
      Money muyCaro = new Money(BigDecimal.valueOf(151.00), "MXN");
      assertThrows(IllegalArgumentException.class, () -> producto.cambiarPrecio(muyCaro),
          "Debe bloquear inflación > 50%");
    }

    @Test
    @DisplayName("Debe permitir aumento válido (<= 50%)")
    void permiteAumentoValido() {
      Money nuevoPrecio = new Money(BigDecimal.valueOf(150.00), "MXN");
      producto.cambiarPrecio(nuevoPrecio);
      assertEquals(nuevoPrecio, producto.getPrecio());
    }
  }

  @Nested
  @DisplayName("Gestión de Imágenes")
  class GestionImagenes {
    private Producto producto;

    @BeforeEach
    void init() {
      producto = Producto.crear("Camara", "Digital", precioDefault, categoriaDefault);
    }

    @Test
    @DisplayName("Máximo 5 imágenes permitidas")
    void limiteImagenes() {
      // Agregar 5 imágenes
      IntStream.range(0, 5).forEach(i -> producto.agregarImagen(new Imagen("http://sitio.com/img" + i, "Alt", i)));

      // Intentar agregar la 6ta
      Imagen extra = new Imagen("http://sitio.com/extra", "Alt", 6);
      assertThrows(IllegalStateException.class, () -> producto.agregarImagen(extra),
          "Debe lanzar excepción al exceder límite");
    }

    @Test
    @DisplayName("Debe agregar imagen válida correctamente")
    void agregarImagenOk() {
      Imagen img = new Imagen("https://img.com/1.png", "Frontal", 1);
      producto.agregarImagen(img);
      assertEquals(1, producto.getImagenes().size());
      assertTrue(producto.getImagenes().contains(img));
    }

    @Test
    @DisplayName("Debe remover imagen por ID")
    void removerImagen() {
      Imagen img1 = new Imagen("https://img.com/1.png", "Frontal", 1);
      Imagen img2 = new Imagen("https://img.com/2.png", "Trasera", 2);
      producto.agregarImagen(img1);
      producto.agregarImagen(img2);
      // Remover una imagen (debe quedar al menos 1)
      producto.removerImagen(img1.getId());
      assertEquals(1, producto.getImagenes().size());
      assertFalse(producto.getImagenes().contains(img1));
      assertTrue(producto.getImagenes().contains(img2));
    }
  }

  @Nested
  @DisplayName("Ciclo de Vida y Activación/Desactivacion")
  class CicloVida {
    private Producto producto;

    @BeforeEach
    void init() {
      producto = Producto.crear("Phone", "Smart", precioDefault, categoriaDefault);
    }

    @Test
    @DisplayName("No activar si no tiene imágenes")
    void falloActivacionSinImagen() {
      assertThrows(IllegalStateException.class, () -> producto.activar(),
          "Producto sin imágenes no debe poder activarse");
    }

    @Test
    @DisplayName("Debe activar si cumple condiciones (Precio > 0 y tiene imagen)")
    void activarExitoso() {
      producto.agregarImagen(new Imagen("http://img.com/1", "Alt", 1));
      // Precio ya es > 0 por creación
      producto.activar();
      assertTrue(producto.isDisponible());
    }

    @Test
    @DisplayName("Desactivar producto activo")
    void desactivarExitoso() {
      producto.agregarImagen(new Imagen("http://img.com/1", "Alt", 1));
      producto.activar();

      producto.desactivar();
      assertFalse(producto.isDisponible());
    }

    @Test
    @DisplayName("Error al desactivar si ya está inactivo")
    void falloDobleDesactivacion() {
      assertFalse(producto.isDisponible());
      assertThrows(IllegalStateException.class, () -> producto.desactivar());
    }
  }
}
