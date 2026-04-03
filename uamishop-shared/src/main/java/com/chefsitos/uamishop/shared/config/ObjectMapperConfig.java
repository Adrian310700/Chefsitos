package com.chefsitos.uamishop.shared.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class ObjectMapperConfig {
  @Bean
  ObjectMapper objectMapper() {
    return new ObjectMapper();
  }
}
