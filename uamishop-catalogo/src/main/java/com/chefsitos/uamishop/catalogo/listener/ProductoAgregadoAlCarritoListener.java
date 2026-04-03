package com.chefsitos.uamishop.catalogo.listener;

import static com.chefsitos.uamishop.shared.util.LogColor.*;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.chefsitos.uamishop.catalogo.service.ProductoEstadisticasService;
import com.chefsitos.uamishop.shared.event.ProductoAgregadoAlCarritoEvent;
import com.chefsitos.uamishop.catalogo.config.RabbitConfig;

import com.chefsitos.uamishop.shared.infraestructure.inbox.InboxEvent;
import com.chefsitos.uamishop.shared.infraestructure.inbox.InboxRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductoAgregadoAlCarritoListener {
  private final ProductoEstadisticasService productoEstadisticasService;
  private final InboxRepository inboxRepository;

  @RabbitListener(queues = RabbitConfig.QUEUE_CATALOGO_PRODUCTO_AGREGADO)
  @EventListener
  @Async // El listener se ejecuta en un hilo distinto, las métricas son eventualmente
         // consistentes
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  // Manejamos una transacción distinta (secundaria) pues una
  // falla aquí no debe afectar ni bloquear la transacción
  // principal
  public void onProductoAgregadoAlCarrito(ProductoAgregadoAlCarritoEvent event) {
    if (event.eventId() != null && inboxRepository.existsById(event.eventId())) {
      log.warn(AZUL + "Evento duplicado omitido (Idempotente): {}" + RESET, event.eventId());
      return;
    }

    log.info(AZUL + "Evento: ProductoAgregadoAlCarrito recibido" + RESET
        + " | productoId={}, carritoId={}, cantidad={}",
        event.productoId(), event.carritoId(), event.cantidad());
    productoEstadisticasService.registrarAgregadoAlCarrito(event.productoId());

    if (event.eventId() != null) {
      inboxRepository.save(InboxEvent.from(event.eventId()));
    }
  }

}
