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
                        .title("UAMIShop API")
                        .version("0.0.3")
                        .description("API REST para la aplicación UAMIShop")
                        .contact(new Contact()
                                .name("Chefsitos")
                                .url("http://localhost:8080")
                                .email("chefsitos@uamishop.com"))
                        .license(new License()
                                .name("API License")
                                .url("http://localhost:8080")));
    }
}
