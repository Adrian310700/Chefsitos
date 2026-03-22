package com.chefsitos.uamishop.ventas.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    public static final String EVENTS_EXCHANGE = "uamishop.events";
    public static final String QUEUE_CARRITO_ORDEN_CREADA = "carrito.orden-creada";
    public static final String RK_ORDEN_CREADA = "orden.creada";

    @Bean
    public TopicExchange eventsExchange() {
        return new TopicExchange(EVENTS_EXCHANGE);
    }

    @Bean
    public Queue carritoOrdenCreadaQueue() {
        return new Queue(QUEUE_CARRITO_ORDEN_CREADA, true);
    }

    @Bean
    public Binding carritoOrdenCreadaBinding(Queue carritoOrdenCreadaQueue, TopicExchange eventsExchange) {
        return BindingBuilder.bind(carritoOrdenCreadaQueue)
                .to(eventsExchange)
                .with(RK_ORDEN_CREADA);
    }

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
