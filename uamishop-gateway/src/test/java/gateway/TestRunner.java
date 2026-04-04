package gateway;

import com.intuit.karate.junit5.Karate;

public class TestRunner {

  @Karate.Test
  Karate testAll() {
    return Karate.run("classpath:features/productos.feature");
  }
}
