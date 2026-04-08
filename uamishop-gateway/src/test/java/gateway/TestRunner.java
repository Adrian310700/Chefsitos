package gateway;

import com.intuit.karate.junit5.Karate;

public class TestRunner {

  @Karate.Test
  Karate testAll() {
    // Para ejecutar todas las clases de pruebas en la carpeta "features", si se
    // quiere probar una clase (feature) en especifico añadir
    // "/nombre_archivo.feature"
    return Karate.run("classpath:features");
  }
}
