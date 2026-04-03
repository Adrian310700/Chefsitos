package com.chefsitos.uamishop.catalogo.config;

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

  /** Cola que recibe mensajes cuando se compra un producto. */
  public static final String QUEUE_CATALOGO_PRODUCTO_COMPRADO = "catalogo.producto-comprado";

  /** Cola que recibe mensajes cuando se agrega un producto al carrito. */
  public static final String QUEUE_CATALOGO_PRODUCTO_AGREGADO = "catalogo.producto-agregado-carrito";

  /**
   * Clave de enrutamiento (routing key) para identificar el evento de producto
   * comprado.
   */
  public static final String RK_PRODUCTO_COMPRADO = "producto.comprado";

  /**
   * Clave de enrutamiento (routing key) para identificar el evento de producto
   * agregado al carrito.
   */
  public static final String RK_PRODUCTO_AGREGADO = "producto.agregado-carrito";

  /**
   *
   * /**
   * Define y crea la cola para los eventos de productos comprados.
   * El segundo parámetro (true) indica que la cola es "duradera" (durable),
   * lo que significa que no se perderá si el servidor de RabbitMQ se reinicia.
   */
  @Bean
  public Queue catalogoProductoCompradoQueue() {
    return new Queue(QUEUE_CATALOGO_PRODUCTO_COMPRADO, true);
  }

  /**
   * Define y crea la cola para los eventos de productos agregados al carrito.
   * También se configura como duradera.
   */
  @Bean
  public Queue catalogoProductoAgregadoQueue() {
    return new Queue(QUEUE_CATALOGO_PRODUCTO_AGREGADO, true);
  }

  /**
   * Crea un enlace (Binding) entre la cola de producto comprado y el exchange.
   * Le indica a RabbitMQ que cualquier mensaje que llegue al exchange con la
   * clave de
   * enrutamiento RK_PRODUCTO_COMPRADO debe ser redirigido a esta cola específica.
   */
  @Bean
  public Binding catalogoProductoCompradoBinding(Queue catalogoProductoCompradoQueue, TopicExchange eventsExchange) {
    return BindingBuilder.bind(catalogoProductoCompradoQueue)
        .to(eventsExchange)
        .with(RK_PRODUCTO_COMPRADO);
  }

  /**
   * Crea un enlace (Binding) entre la cola de producto agregado al carrito y el
   * exchange.
   * Funciona igual que el anterior, pero con su respectiva clave de enrutamiento.
   */
  @Bean
  public Binding catalogoProductoAgregadoBinding(Queue catalogoProductoAgregadoQueue, TopicExchange eventsExchange) {
    return BindingBuilder.bind(catalogoProductoAgregadoQueue)
        .to(eventsExchange)
        .with(RK_PRODUCTO_AGREGADO);
  }

  /**
   * Define el conversor de mensajes.
   * Permite que los objetos Java (eventos) se conviertan automáticamente a
   * formato JSON
   * al enviarse a RabbitMQ, y viceversa al recibirse.
   */
  @Bean
  public JacksonJsonMessageConverter jacksonJsonMessageConverter() {
    return new JacksonJsonMessageConverter();
  }

  /**
   * Configura la plantilla principal de RabbitMQ (RabbitTemplate).
   * Es la herramienta que se utiliza en el código para enviar los mensajes.
   * Se le inyecta el conversor JSON configurado anteriormente para asegurar que
   * todo se envíe en dicho formato.
   */
  @Bean
  public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
      JacksonJsonMessageConverter messageConverter) {
    RabbitTemplate template = new RabbitTemplate(connectionFactory);
    template.setMessageConverter(messageConverter);
    return template;
  }
}
