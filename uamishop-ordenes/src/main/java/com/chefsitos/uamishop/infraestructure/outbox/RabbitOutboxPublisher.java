package com.chefsitos.uamishop.infraestructure.outbox;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import com.chefsitos.uamishop.shared.infraestructure.outbox.OutboxEvent;
import com.chefsitos.uamishop.shared.infraestructure.outbox.OutboxMessagePublisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitOutboxPublisher implements OutboxMessagePublisher {

  private final RabbitTemplate rabbitTemplate;

  public void publish(OutboxEvent event) {
    Message message = MessageBuilder.withBody(event.getPayload().getBytes())
        .setContentType(MessageProperties.CONTENT_TYPE_JSON)
        .build();

    rabbitTemplate.send(event.getExchange(), event.getRoutingKey(), message);

    log.info("RabbitOutbox: mensaje publicado | exchange={}, routingKey={}, eventId={}",
        event.getExchange(), event.getRoutingKey(), event.getId());
  }

}
