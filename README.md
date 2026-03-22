# Chefsitos

Repositorio para almacenar el proyecto UAMIShop de la UEA Temas Selectos de Ingeniería de Software

## Arquitectura

El proyecto está compuesto por dos microservicios:

| Servicio             | Puerto |
| -------------------- | ------ |
| `uamishop-principal` | 8080   |
| `uamishop-catalogo`  | 8081   |

**Infraestructura:**

| Servicio | Puerto       | Descripción                                      |
| -------- | ------------ | ------------------------------------------------ |
| MySQL    | 3306         | Base de datos (`uamishop` y `uamishop_catalogo`) |
| RabbitMQ | 5672 / 15672 | Mensajería asíncrona / Consola de administración |

## Requisitos

- Java 21 (se puede utilizar [sdkman](https://sdkman.io/))
- Docker y Docker Compose

## Instalación y configuración

1. Clonar el repositorio
2. `cd Chefsitos`
3. Si están usando Linux/MacOS: `chmod +x uamishop-principal/mvnw uamishop-catalogo/mvnw`

## Ejecución

### Opción 1: Sin contenedores

Levantar solo la infraestructura (MySQL + RabbitMQ) en Docker y correr los microservicios localmente:

```bash
# 1. Levantar infraestructura
docker compose up -d mysql rabbitmq

# 2. Esperar a que MySQL esté healthy
docker compose ps

# 3. Arrancar el monolito principal (Terminal 1)
cd uamishop-principal
./mvnw spring-boot:run -Dspring-boot.run.profiles=mysql -DskipTests

# 4. Arrancar el microservicio de catálogo (Terminal 2)
cd uamishop-catalogo
./mvnw spring-boot:run -DskipTests
```

### Opción 2: Todo en Docker

Levantar todo el stack (infraestructura + microservicios) containerizado:

```bash
# Construir y levantar todo
docker compose up -d --build

# Ver logs
docker compose logs -f
```

> **Nota:** No se pueden usar ambas opciones simultáneamente ya que los puertos (8080, 8081) estarían en conflicto.

### Detener servicios

```bash
# Detener contenedores
docker compose down

# Detener contenedores y borrar datos persistidos (MySQL)
docker compose down -v
```

### Tests

```bash
# uamishop-principal
cd uamishop-principal
./mvnw test

# uamishop-catalogo
cd uamishop-catalogo
./mvnw test
```

## Endpoints

Una vez en ejecución:

- **Principal:** [http://localhost:8080/](http://localhost:8080/)
- **Principal Swagger:** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
---
- **Catálogo API:** [http://localhost:8081/](http://localhost:8081/)
- **Catálogo Swagger:** [http://localhost:8081/swagger-ui.html](http://localhost:8081/swagger-ui.html)

---
- **RabbitMQ Admin:** [http://localhost:15672/](http://localhost:15672/) (guest/guest)

## Extensiones recomendadas para VSCode

Se encuentra el pack de extensiones recomendado en el archivo `.vscode/extensions.json` del proyecto. Se recomienda su instalación para una mejor experiencia de desarrollo.

## Estándar de codificación

[https://google.github.io/styleguide/javaguide.html](https://google.github.io/styleguide/javaguide.html)
