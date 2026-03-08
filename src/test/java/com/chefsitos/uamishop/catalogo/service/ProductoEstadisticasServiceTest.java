package com.chefsitos.uamishop.catalogo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.InvalidDataAccessApiUsageException;

import com.chefsitos.uamishop.catalogo.domain.ProductoEstadisticas;
import com.chefsitos.uamishop.catalogo.repository.ProductoEstadisticasJpaRepository;
import com.chefsitos.uamishop.shared.exception.ResourceNotFoundException;

import jakarta.transaction.Transactional;

@SpringBootTest
@Transactional
class ProductoEstadisticasServiceTest {

  @Autowired
  private ProductoEstadisticasService estadisticasService;

  @Autowired
  private ProductoEstadisticasJpaRepository estadisticasRepository;

  // helpers
  private void crearEstadisticaEnBD(UUID productoId, int ventasTotales, int cantidadVendida, int vecesAgregadoAlCarrito,
      Instant ultimaVentaAt, Instant ultimaAgregadoAlCarritoAt) {
    ProductoEstadisticas estadisticas = new ProductoEstadisticas(productoId, ventasTotales, cantidadVendida,
        vecesAgregadoAlCarrito, ultimaVentaAt, ultimaAgregadoAlCarritoAt);
    // Persistir estadistica creada
    estadisticasRepository.save(estadisticas);
  }

  private void crearVariasEstadisticasEnBD(int p1Ventas, int p2Ventas, int p3Ventas) {
    ProductoEstadisticas p1 = new ProductoEstadisticas(
        UUID.randomUUID(), 1, p1Ventas, 0, Instant.now(), null);

    ProductoEstadisticas p2 = new ProductoEstadisticas(
        UUID.randomUUID(), 1, p2Ventas, 0, Instant.now(), null);

    if (p3Ventas > 0) {
      ProductoEstadisticas p3 = new ProductoEstadisticas(
          UUID.randomUUID(), 1, 20, 0, Instant.now(), null);
      estadisticasRepository.saveAll(List.of(p1, p2, p3));
    } else {
      estadisticasRepository.saveAll(List.of(p1, p2));
    }
  }

  @Nested
  class RegistrarVenta {
    // Caso de exito 1
    @Test
    @DisplayName("Registrar la venta creando nueva estadistica")
    void registrarVenta_CrearEstadisticaNueva_Correctamente() {
      // Cualquier producto
      UUID productoId = UUID.randomUUID();
      int cantidad = 5;
      // Crea una nueva estadistica y registra la venta
      estadisticasService.registrarVenta(productoId, cantidad);
      // Recuperar estadisticas
      ProductoEstadisticas estadisticas = estadisticasRepository.findById(productoId).orElseThrow();
      // Comprobar que coincide y se registro la fecha de ultima venta
      assertEquals(productoId, estadisticas.getProductoId());
      assertEquals(1, estadisticas.getVentasTotales());
      assertEquals(cantidad, estadisticas.getCantidadVendida());
      assertNotNull(estadisticas.getUltimaVentaAt());
    }

    // Caso de exito 2
    @Test
    @DisplayName("crea estadistica previamente y registra venta")
    void registrarVenta_EstadisticaExistente_Correctamente() {

      UUID productoId = UUID.randomUUID();
      // Persistir estadistica antes
      crearEstadisticaEnBD(productoId, 2, 10, 0, null, null);
      // Encuentra la estadistica y registra la venta
      estadisticasService.registrarVenta(productoId, 3);

      ProductoEstadisticas actualizado = estadisticasRepository.findById(productoId).orElseThrow();

      assertEquals(3, actualizado.getVentasTotales());
      assertEquals(13, actualizado.getCantidadVendida());
      assertNotNull(actualizado.getUltimaVentaAt());
    }

    // Caso de error
    @Test
    @DisplayName("Pasa id nulo y devuelve InvalidDataAccesApiUsageException")
    void registrarVenta_productoIdNulo_Excepcion() {
      // Comprueba el tipo de excepcion
      assertThrows(InvalidDataAccessApiUsageException.class,
          () -> estadisticasService.registrarVenta(null, 5));
    }
  }

  @Nested
  @DisplayName("crea estadistica previamente y registra agregado al carrito")
  class RegistrarAgregadoAlCarrito {
    @Test
    void registrarAgregadoAlCarrito_EstadisticaExistente_Correctamente() {

      UUID productoId = UUID.randomUUID();
      // Estadistica previamente creada
      crearEstadisticaEnBD(productoId, 0, 0, 2, null, null);
      // Recupera estadistica y actualiza agregado al carrito
      estadisticasService.registrarAgregadoAlCarrito(productoId);

      ProductoEstadisticas resultado = estadisticasRepository.findById(productoId).orElseThrow();

      assertEquals(3, resultado.getVecesAgregadoAlCarrito());
      assertNotNull(resultado.getUltimaAgregadoAlCarritoAt());
    }

    @Test
    @DisplayName("Crea nueva estadistica al registrar agregado al carrito")
    void registrarAgregadoAlCarrito_DesdeProductoNuevo_Correctamente() {

      UUID productoId = UUID.randomUUID();
      // Crea nueva estadistica y registra agregado al carrito
      estadisticasService.registrarAgregadoAlCarrito(productoId);

      ProductoEstadisticas resultado = estadisticasRepository.findById(productoId).orElseThrow();

      assertEquals(productoId, resultado.getProductoId());
      assertEquals(1, resultado.getVecesAgregadoAlCarrito());
      assertNotNull(resultado.getUltimaAgregadoAlCarritoAt());
    }
  }

  @Nested
  class ObtenerMasVendidos {
    @Test
    @DisplayName("Obtener estadisticas de mas vendidos y comprueba el orden")
    void obtenerMasVendidos_Ordenados_Correctamente() {
      // Persistir varias estadisticas con diferentes cantidades de ventas
      crearVariasEstadisticasEnBD(10, 30, 20);

      List<ProductoEstadisticas> resultado = estadisticasService.obtenerMasVendidos(3);

      assertEquals(3, resultado.size());
      // Comprueba que el orden es correcto
      assertEquals(30, resultado.get(0).getCantidadVendida());
      assertEquals(20, resultado.get(1).getCantidadVendida());
      assertEquals(10, resultado.get(2).getCantidadVendida());
    }

    @Test
    @DisplayName("Devuelve solo el numero limite de estadisticas")
    void obtenerMasVendidos_ComprobarLimiteMenor_Correctamente() {

      crearVariasEstadisticasEnBD(50, 40, 30);

      List<ProductoEstadisticas> resultado = estadisticasService.obtenerMasVendidos(2);

      assertEquals(2, resultado.size());

      assertEquals(50, resultado.get(0).getCantidadVendida());
      assertEquals(40, resultado.get(1).getCantidadVendida());
    }

    @Test
    @DisplayName("Devuelve todas las estadisticas al pasar numero limite mayor a los registros")
    void obtenerMasVendidos_ComprobarLimiteMayor_Correctamente() {

      crearVariasEstadisticasEnBD(10, 30, 0);

      List<ProductoEstadisticas> resultado = estadisticasService.obtenerMasVendidos(10);

      assertEquals(2, resultado.size());
    }
    // No hay casos de error para probar excepciones
  }

  @Nested
  class ObtenerEstadisticas {
    @Test
    @DisplayName("Obtener estadisticas persistidas previamente")
    void obtenerEstadisticas_Correctamente() {

      UUID productoId = UUID.randomUUID();

      crearEstadisticaEnBD(productoId, 5, 20, 3, Instant.now(), Instant.now());

      ProductoEstadisticas resultado = estadisticasService.obtenerEstadisticas(productoId);

      assertNotNull(resultado);
      assertEquals(productoId, resultado.getProductoId());
      assertEquals(5, resultado.getVentasTotales());
      assertEquals(20, resultado.getCantidadVendida());
      assertEquals(3, resultado.getVecesAgregadoAlCarrito());
    }

    @Test
    @DisplayName("Devuelve ResourceNotFoundException")
    void obtenerEstadisticas_ProductoInexistente_Excepcion() {

      UUID productoId = UUID.randomUUID();

      ResourceNotFoundException exception = assertThrows(
          ResourceNotFoundException.class,
          () -> estadisticasService.obtenerEstadisticas(productoId));
      // Comprueba que se obtiene el id correcto del producto en el mensaje de la
      // excepcion
      assertTrue(exception.getMessage().contains(productoId.toString()));
    }
  }

}
