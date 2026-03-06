package com.chefsitos.uamishop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class UamishopApplication {

  public static void main(String[] args) {
    SpringApplication.run(UamishopApplication.class, args);
  }

}
