package com.chefsitos.uamishop.shared.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración compartida de RabbitMQ.
 * Define centralmente el Exchange principal (TopicExchange) para que todo
 * el ecosistema de microservicios utilice el mismo canal de publicación.
 */
@Configuration
public class RabbitExchangeConfig {

  public static final String EVENTS_EXCHANGE = "uamishop.events";

  @Bean
  public TopicExchange eventsExchange() {
    return new TopicExchange(EVENTS_EXCHANGE);
  }
}
