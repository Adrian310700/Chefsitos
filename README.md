# Chefsitos

Es el repositorio para almacenar el proyecto de la UEA Temas Selectos de Ingeniería de Software

## Requisitos

- Java 21 (se puede utilizar [sdkman](https://sdkman.io/))

## Instalación y configuración

1. Clonar el repositorio
2. cd Chefsitos
3. Si están usando Linux/MacOS: `chmod +x mvnw`

## Ejecución

Se pueden utilizar las opciones del IDE o los siguientes comandos:

### Modo desarrollo

- Windows: `.\mvnw spring-boot:run`
- Linux/Mac: `./mvnw spring-boot:run`

### Producción

- Windows: `.\mvnw clean package`
- Linux/Mac: `./mvnw clean package`

### Test

- Windows: `.\mvnw test`
- Linux/Mac: `./mvnw test`

## Extensiones recomendadas para VSCode

Se encuentra el pack de extensiones recomendado en el archivo `.vscode/extensions.json` del proyecto. Se recomienda su instalación para una mejor experiencia de desarrollo.

## Estándar de codificación

[https://google.github.io/styleguide/javaguide.html](https://google.github.io/styleguide/javaguide.html)

## Desarrollo

Cuando la aplicación esté en ejecución, se puede acceder a la API a través de:
[http://localhost:8080/](http://localhost:8080/). Tenemos un endpoint de prueba en [http://localhost:8080/hello](http://localhost:8080/hello).
