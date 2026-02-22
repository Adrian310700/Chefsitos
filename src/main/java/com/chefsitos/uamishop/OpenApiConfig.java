package com.chefsitos.uamishop;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI customOpenAPI() {
    return new OpenAPI()
      .info(new Info()
        .title("API REST - UamiShop Órdenes")
        .version("1.0")
        .description("API del Bounded Context Órdenes - TSIs")
        .contact(new Contact()
          .name("Equipo UamiShop")
          .email("contacto@uamishop.com"))
        .license(new License()
          .name("API License")
          .url("https://uam.mx")));
  }
}
