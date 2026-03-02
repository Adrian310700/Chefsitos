package com.chefsitos.uamishop.ordenes.api;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.chefsitos.uamishop.catalogo.domain.aggregate.Producto;
import com.chefsitos.uamishop.catalogo.domain.entity.Categoria;
import com.chefsitos.uamishop.catalogo.domain.valueObject.CategoriaId;
import com.chefsitos.uamishop.catalogo.repository.CategoriaJpaRepository;
import com.chefsitos.uamishop.catalogo.repository.ProductoJpaRepository;
import com.chefsitos.uamishop.ordenes.api.dto.OrdenDTO;
import com.chefsitos.uamishop.ordenes.controller.dto.DireccionEnvioRequest;
import com.chefsitos.uamishop.ordenes.controller.dto.OrdenRequest;
import com.chefsitos.uamishop.ordenes.controller.dto.OrdenRequest.ItemOrdenRequest;
import com.chefsitos.uamishop.ordenes.domain.valueObject.OrdenId;
import com.chefsitos.uamishop.ordenes.repository.OrdenJpaRepository;
import com.chefsitos.uamishop.ordenes.service.OrdenService;
import com.chefsitos.uamishop.shared.domain.valueObject.Money;
import com.chefsitos.uamishop.shared.exception.BusinessRuleException;
import com.chefsitos.uamishop.shared.exception.ResourceNotFoundException;

/**
 * Pruebas de integración sobre la API interna del módulo Ordenes (OrdenesApi).
 *
 * <p>
 * Se prueba directamente la interfaz {@Link OrdenesApi} sin usar
 * TestRestTemplate
 * ni llamadas HTTP. Esto valida el comportamiento del módulo a nivel de API
 * inter-módulo, tal como lo consumirían otros módulos en la arquitectura
 * monolito modular.
 * </p>
 *
 * <p>
 * Las órdenes se crean usando OrdenService.crear() (método interno del módulo),
 * ya que la creación directa es responsabilidad del controller REST, no de la
 * API inter-módulo.
 * </p>
 */
@SpringBootTest
@DisplayName("OrdenesApi – Pruebas de integración sobre la API interna del módulo")
class OrdenesApiIntegrationTest {

  @Autowired
  private OrdenesApi ordenesApi;

  @Autowired
  private OrdenService ordenService;

  @Autowired
  private OrdenJpaRepository ordenRepository;

  @Autowired
  private ProductoJpaRepository productoRepository;

  @Autowired
  private CategoriaJpaRepository categoriaRepository;

  private UUID productoId;

  @BeforeEach
  void setUp() {
    // 1) Crea y guarda una categoría (esto normalmente no cambia)
    UUID categoriaUUID = UUID.randomUUID();
    CategoriaId categoriaId = new CategoriaId(categoriaUUID);

    Categoria categoria = Categoria.crear(
        categoriaId,
        "Electrónicos",
        "Dispositivos electrónicos");
    categoriaRepository.save(categoria);

    // 2) Crea y guarda un producto (firma puede variar entre proyectos/equipos)
    Producto producto = crearProductoCompatible(categoria, categoriaId);
    productoRepository.save(producto);

    // 3) Obtiene el UUID del producto aunque cambie el getter de ID
    this.productoId = extraerUuidProductoId(producto);
    assertNotNull(this.productoId, "No se pudo extraer el UUID del ProductoId desde Producto");
  }

  @AfterEach
  void cleanUp() {
    ordenRepository.deleteAll();
    productoRepository.deleteAll();
    categoriaRepository.deleteAll();
  }

  /**
   * Crea una orden usando el método interno del servicio (vía controller REST
   * flow).
   */
  private OrdenDTO givenOrdenCreada() {
    OrdenRequest request = new OrdenRequest(
        UUID.randomUUID(),
        new DireccionEnvioRequest(
            "María García",
            "Av. Insurgentes 123",
            "Ciudad de México",
            "CDMX",
            "06600",
            "5598765432",
            "Entregar en recepción"),
        List.of(new ItemOrdenRequest(productoId.toString(), new BigDecimal("2"))));

    UUID id = ordenService.crear(request).id();

    return OrdenDTO.from(
        ordenRepository.findById(new OrdenId(id))
            .orElseThrow());
  }

  /**
   * Crea un Producto sin depender de la firma exacta de Producto.crear(...).
   * Intenta encontrar un método estático "crear" y armar argumentos compatibles
   * con los tipos esperados (String, BigDecimal, Money, Categoria, CategoriaId,
   * UUID, etc.).
   */
  private Producto crearProductoCompatible(Categoria categoria, CategoriaId categoriaId) {
    String nombre = "Audífonos Bluetooth";
    String descripcion = "Audífonos inalámbricos con cancelación de ruido";
    BigDecimal precio = new BigDecimal("1500.00");
    String moneda = "MXN";
    Money money = new Money(precio, moneda);

    try {
      Method[] methods = Producto.class.getDeclaredMethods();

      for (Method m : methods) {
        if (!m.getName().equals("crear"))
          continue;
        if (!java.lang.reflect.Modifier.isStatic(m.getModifiers()))
          continue;

        Class<?>[] p = m.getParameterTypes();
        Object[] args = new Object[p.length];

        boolean ok = true;
        int stringIdx = 0;

        for (int i = 0; i < p.length; i++) {
          Class<?> t = p[i];

          if (t.equals(String.class)) {
            // normalmente hay 2 strings (nombre, descripción) y a veces una moneda extra
            if (stringIdx == 0)
              args[i] = nombre;
            else if (stringIdx == 1)
              args[i] = descripcion;
            else
              args[i] = moneda;
            stringIdx++;
          } else if (t.equals(BigDecimal.class)) {
            args[i] = precio;
          } else if (t.equals(Money.class)) {
            args[i] = money;
          } else if (t.equals(Categoria.class)) {
            args[i] = categoria;
          } else if (t.equals(CategoriaId.class)) {
            args[i] = categoriaId;
          } else if (t.equals(UUID.class)) {
            args[i] = UUID.randomUUID();
          } else {
            // Probable VO tipo ProductoId, etc. Intentamos construirlo con UUID.
            Object vo = construirValueObjectConUuid(t);
            if (vo == null) {
              ok = false;
              break;
            }
            args[i] = vo;
          }
        }

        if (!ok)
          continue;

        m.setAccessible(true);
        Object created = m.invoke(null, args);
        if (created instanceof Producto producto) {
          return producto;
        }
      }

      throw new IllegalStateException(
          "No se encontró una firma compatible para Producto.crear(...). " +
              "Revisa las firmas del método en el módulo catálogo.");

    } catch (Exception e) {
      throw new IllegalStateException("Error creando Producto por reflexión: " + e.getMessage(), e);
    }
  }

  /**
   * Intenta construir un Value Object con un UUID vía constructor(UUID) o
   * valueOf(UUID).
   */
  private Object construirValueObjectConUuid(Class<?> voType) {
    try {
      // constructor(UUID)
      for (Constructor<?> c : voType.getDeclaredConstructors()) {
        Class<?>[] p = c.getParameterTypes();
        if (p.length == 1 && p[0].equals(UUID.class)) {
          c.setAccessible(true);
          return c.newInstance(UUID.randomUUID());
        }
      }

      // static valueOf(UUID)
      try {
        Method valueOf = voType.getDeclaredMethod("valueOf", UUID.class);
        if (java.lang.reflect.Modifier.isStatic(valueOf.getModifiers())) {
          valueOf.setAccessible(true);
          return valueOf.invoke(null, UUID.randomUUID());
        }
      } catch (NoSuchMethodException ignored) {
        // no-op
      }

      return null;
    } catch (Exception e) {
      return null;
    }
  }

  /**
   * Extrae el UUID del id del Producto aunque cambie el nombre del getter:
   * getProductoId(), productoId(), id(), getId(), idProducto(), etc.
   * Si regresa un VO, intenta llamar valor().
   */
  private UUID extraerUuidProductoId(Producto producto) {
    String[] candidatos = {
        "getProductoId",
        "productoId",
        "id",
        "getId",
        "idProducto",
        "getIdProducto"
    };

    for (String name : candidatos) {
      try {
        Method m = producto.getClass().getMethod(name);
        Object idObj = m.invoke(producto);

        if (idObj == null)
          continue;
        if (idObj instanceof UUID uuid)
          return uuid;

        // VO típico con .valor() -> UUID
        try {
          Method valor = idObj.getClass().getMethod("valor");
          Object uuidObj = valor.invoke(idObj);
          if (uuidObj instanceof UUID uuid)
            return uuid;
        } catch (NoSuchMethodException ignored) {
          // no-op
        }

      } catch (NoSuchMethodException ignored) {
        // no-op
      } catch (Exception e) {
        // si falla un candidato, probamos el siguiente
      }
    }

    return null;
  }

  // ─────────────────────────────────────────────────────────────────────────
  // Tests
  // ─────────────────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("buscarPorId()")
  class BuscarPorId {

    @Test
    @DisplayName("retorna la orden correcta cuando existe")
    void buscarPorId_retornaOrdenExistente() {
      OrdenDTO creada = givenOrdenCreada();

      OrdenDTO encontrada = ordenesApi.buscarPorId(creada.idOrden());

      assertNotNull(encontrada);
      assertEquals(creada.idOrden(), encontrada.idOrden());
      assertEquals(creada.numeroOrden(), encontrada.numeroOrden());
      assertEquals("PENDIENTE", encontrada.estado());
    }

    @Test
    @DisplayName("lanza ResourceNotFoundException si la orden no existe")
    void buscarPorId_ordenInexistente_lanzaExcepcion() {
      assertThrows(ResourceNotFoundException.class,
          () -> ordenesApi.buscarPorId(UUID.randomUUID()));
    }
  }

  @Nested
  @DisplayName("buscarTodas()")
  class BuscarTodas {

    @Test
    @DisplayName("retorna lista con todas las órdenes creadas")
    void buscarTodas_retornaListaConOrdenes() {
      givenOrdenCreada();
      givenOrdenCreada();

      List<OrdenDTO> ordenes = ordenesApi.buscarTodas();

      assertNotNull(ordenes);
      assertTrue(ordenes.size() >= 2);
    }

    @Test
    @DisplayName("retorna lista vacía si no hay órdenes")
    void buscarTodas_sinOrdenes_retornaListaVacia() {
      List<OrdenDTO> ordenes = ordenesApi.buscarTodas();

      assertNotNull(ordenes);
      assertTrue(ordenes.isEmpty());
    }

    @Test
    @DisplayName("cada OrdenDTO en la lista contiene sus ítems")
    void buscarTodas_cadaOrdenContieneItems() {
      givenOrdenCreada();

      List<OrdenDTO> ordenes = ordenesApi.buscarTodas();

      ordenes.forEach(orden -> assertFalse(orden.items().isEmpty()));
    }
  }

  @Nested
  @DisplayName("confirmarOrden()")
  class ConfirmarOrden {

    @Test
    @DisplayName("confirma una orden en estado PENDIENTE y retorna estado CONFIRMADA")
    void confirmarOrden_cambiaEstadoAConfirmada() {
      OrdenDTO creada = givenOrdenCreada();

      OrdenDTO confirmada = ordenesApi.confirmarOrden(creada.idOrden());

      assertNotNull(confirmada);
      assertEquals("CONFIRMADA", confirmada.estado());
      assertEquals(creada.idOrden(), confirmada.idOrden());
    }

    @Test
    @DisplayName("lanza BusinessRuleException si se intenta confirmar una orden ya CONFIRMADA")
    void confirmarOrden_yaConfirmada_lanzaBusinessRuleException() {
      OrdenDTO creada = givenOrdenCreada();
      ordenesApi.confirmarOrden(creada.idOrden());

      assertThrows(BusinessRuleException.class,
          () -> ordenesApi.confirmarOrden(creada.idOrden()));
    }

    @Test
    @DisplayName("lanza ResourceNotFoundException si la orden no existe")
    void confirmarOrden_inexistente_lanzaResourceNotFoundException() {
      assertThrows(ResourceNotFoundException.class,
          () -> ordenesApi.confirmarOrden(UUID.randomUUID()));
    }
  }

  @Nested
  @DisplayName("cancelarOrden()")
  class CancelarOrden {

    @Test
    @DisplayName("cancela una orden PENDIENTE con motivo válido")
    void cancelarOrden_conMotivoValido_cambiaEstadoACancelada() {
      OrdenDTO creada = givenOrdenCreada();

      OrdenDTO cancelada = ordenesApi.cancelarOrden(
          creada.idOrden(), "El cliente ya no requiere el producto");

      assertNotNull(cancelada);
      assertEquals("CANCELADA", cancelada.estado());
    }

    @Test
    @DisplayName("cancela una orden CONFIRMADA con motivo válido")
    void cancelarOrden_confirmada_cambiaEstadoACancelada() {
      OrdenDTO creada = givenOrdenCreada();
      ordenesApi.confirmarOrden(creada.idOrden());

      OrdenDTO cancelada = ordenesApi.cancelarOrden(
          creada.idOrden(), "El cliente cambió de opinión después de confirmar");

      assertEquals("CANCELADA", cancelada.estado());
    }

    @Test
    @DisplayName("lanza BusinessRuleException si el motivo tiene menos de 10 caracteres")
    void cancelarOrden_conMotivoCorto_lanzaBusinessRuleException() {
      OrdenDTO creada = givenOrdenCreada();

      assertThrows(BusinessRuleException.class,
          () -> ordenesApi.cancelarOrden(creada.idOrden(), "Corto"));
    }

    @Test
    @DisplayName("lanza ResourceNotFoundException si la orden no existe")
    void cancelarOrden_inexistente_lanzaResourceNotFoundException() {
      assertThrows(ResourceNotFoundException.class,
          () -> ordenesApi.cancelarOrden(UUID.randomUUID(), "Motivo suficientemente largo"));
    }
  }
}
