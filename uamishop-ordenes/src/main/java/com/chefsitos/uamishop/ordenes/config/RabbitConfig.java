package com.chefsitos.uamishop.ordenes.config;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

  public static final String RK_PRODUCTO_COMPRADO = "producto.comprado";
  public static final String RK_ORDEN_CREADA = "orden.creada";

  // El TopicExchange "eventsExchange" ahora es proveído por
  // SharedRabbitExchangeConfig en uamishop-shared

  // Las colas (Queues) de los consumidores NO deben declararse aquí.
  // Quien consume el mensaje (Ventas, Catálogo) es el responsable de declarar su
  // propia Queue.
  // Órdenes solo necesita declarar el Exchange.

  @Bean
  public JacksonJsonMessageConverter jacksonJsonMessageConverter() {
    return new JacksonJsonMessageConverter();
  }

  @Bean
  public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
      JacksonJsonMessageConverter messageConverter) {
    RabbitTemplate template = new RabbitTemplate(connectionFactory);
    template.setMessageConverter(messageConverter);
    return template;
  }
}
