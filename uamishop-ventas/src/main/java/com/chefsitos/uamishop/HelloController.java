package com.chefsitos.uamishop;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

  @GetMapping("/")
  public String greeting1() {
    return "Ruta: /: Hola mundo, desde Spring";
  }

  @GetMapping("/hello")
  public String greeting2() {
    return "Ruta: /hello: Hola mundo, desde Spring";
  }
}
