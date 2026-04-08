package com.chefsitos.uamishop.shared.infraestructure.outbox;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnBean(RabbitTemplate.class)
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
