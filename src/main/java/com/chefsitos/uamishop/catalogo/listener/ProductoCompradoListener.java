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
    log.info(AZUL + "Evento: ProductoComprado recibido. Se crean estadisticas" + RESET
        + " | ordenId={}, clienteId={}, totalItems={}",
        event.ordenId(), event.clienteId(), event.items().size());
    event.items().forEach(item -> productoEstadisticasService.registrarVenta(item.productoId(), item.cantidad()));
  }

}
