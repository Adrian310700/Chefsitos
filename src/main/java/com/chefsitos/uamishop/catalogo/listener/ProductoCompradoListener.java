package com.chefsitos.uamishop.catalogo.listener;

import static com.chefsitos.uamishop.shared.util.LogColor.*;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.chefsitos.uamishop.catalogo.service.ProductoEstadisticasService;
import com.chefsitos.uamishop.shared.event.ProductoCompradoEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductoCompradoListener {
  private final ProductoEstadisticasService productoEstadisticasService;

  @EventListener
  @Async // El listener se ejecuta en un hilo distinto, las métricas son eventualmente
         // consistentes
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  // Manejamos una transacción distinta (secundaria) pues una
  // falla aquí no debe afectar ni bloquear la transacción
  // principal
  public void onProductoComprado(ProductoCompradoEvent event) {
    log.info(VERDE + "Evento ProductoComprado recibido | ordenId={}, clienteId={}, totalItems={}" + RESET,
        event.ordenId(), event.clienteId(), event.items().size());
    try {
      event.items().forEach(item -> {
        log.debug(VERDE + "Registrando venta | productoId={}, cantidad={}" + RESET, item.productoId(),
            item.cantidad());
        productoEstadisticasService.registrarVenta(item.productoId(), item.cantidad());
      });
      log.info(VERDE + "Estadisticas de venta registradas | ordenId={}, itemsProcesados={}" + RESET,
          event.ordenId(), event.items().size());
    } catch (Exception e) {
      log.error(ROJO + "Error al registrar estadisticas de venta | ordenId={}, error={}" + RESET,
          event.ordenId(), e.getMessage(), e);
      throw e;
    }
  }

}
