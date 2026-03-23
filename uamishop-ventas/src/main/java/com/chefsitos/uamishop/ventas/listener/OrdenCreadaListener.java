package com.chefsitos.uamishop.ventas.listener;

import static com.chefsitos.uamishop.shared.util.LogColor.*;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.chefsitos.uamishop.shared.event.OrdenCreadaEvent;
import com.chefsitos.uamishop.ventas.config.RabbitConfig;
import com.chefsitos.uamishop.ventas.service.CarritoService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrdenCreadaListener {
  private final CarritoService carritoService;

  @RabbitListener(queues = RabbitConfig.QUEUE_CARRITO_ORDEN_CREADA)
  @EventListener
  @Async // El listener se ejecuta en un hilo distinto, las métricas son eventualmente
         // consistentes
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  // Manejamos una transacción distinta (secundaria) pues una
  // falla aquí no debe afectar ni bloquear la transacción
  // principal
  public void onOrdenCreada(OrdenCreadaEvent event) {
    log.info(AZUL + "Evento: OrdenCreada recibido. Se completa Checkout" + RESET
        + " | ordenId={}, carritoId={}, clienteId={}",
        event.ordenId(), event.carritoId(), event.clienteId());
    carritoService.completarCheckout(event.carritoId());
  }

}
