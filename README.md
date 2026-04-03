# UAMIShop — Chefsitos

Repositorio para almacenar el proyecto UAMIShop de la UEA Temas Selectos de Ingeniería de Software

## Arquitectura

| Servicio            | Puerto | Descripción                    |
| ------------------- | ------ | ------------------------------ |
| `uamishop-gateway`  | 8080   | API Gateway (punto de entrada) |
| `uamishop-catalogo` | 8081   | Productos y categorías         |
| `uamishop-ordenes`  | 8082   | Gestión de órdenes             |
| `uamishop-ventas`   | 8083   | Carritos de compra             |

**Infraestructura:**

| Servicio | Puerto       | Descripción                             |
| -------- | ------------ | --------------------------------------- |
| MySQL    | 3306         | Bases de datos por microservicio        |
| RabbitMQ | 5672 / 15672 | Mensajería asíncrona / Consola de admin |

## Requisitos

- Java 21 (se puede utilizar [sdkman](https://sdkman.io/))
- Maven
- Docker y Docker Compose

## Instalación y configuración

1. Clonar el repositorio
2. `cd Chefsitos`
3. Si están usando Linux/MacOS: `chmod +x uamishop-catalogo/mvnw uamishop-ordenes/mvnw uamishop-ventas/mvnw`
4. Desde la raíz del proyecto, ejecutar `mvn clean install -DskipTests` para instalar el paquete shared en los otros módulos como dependencia.

## Ejecución

### Opción 1: Todo en Docker (recomendada)

```bash
docker compose up -d --build
```

### Opción 2: Solo infraestructura en Docker

```bash
# 1. Levantar infraestructura
docker compose up -d mysql rabbitmq

# 2. Esperar a que MySQL esté healthy
docker compose ps

# 3. Cada microservicio en su terminal
cd uamishop-catalogo && ./mvnw spring-boot:run -DskipTests
cd uamishop-ordenes  && ./mvnw spring-boot:run -DskipTests
cd uamishop-ventas   && ./mvnw spring-boot:run -DskipTests

# 4. API Gateway (con perfil local para usar localhost)
cd uamishop-gateway  && ./mvnw spring-boot:run -DskipTests -Dspring-boot.run.profiles=local
```

> [!NOTE]: No usar ambas opciones simultáneamente porque los puertos estarán en conflicto.

### Detener servicios

```bash
docker compose down        # Detener contenedores
docker compose down -v     # Detener y borrar datos de MySQL
```

### Tests

```bash
# uamishop-catalogo
cd uamishop-catalogo && ./mvnw test

# uamishop-ordenes
cd uamishop-ordenes && ./mvnw test

# uamishop-ventas
cd uamishop-ventas && ./mvnw test
```

## Endpoints

**Vía Gateway (puerto 8080):**

| Ruta                    | Servicio destino |
| ----------------------- | ---------------- |
| `/api/v1/productos/**`  | Catálogo (8081)  |
| `/api/v1/categorias/**` | Catálogo (8081)  |
| `/api/v1/ordenes/**`    | Órdenes (8082)   |
| `/api/v1/carritos/**`   | Ventas (8083)    |

**Swagger UI directo de cada servicio:**

- Catálogo: http://localhost:8081/swagger-ui.html
- Órdenes: http://localhost:8082/swagger-ui.html
- Ventas: http://localhost:8083/swagger-ui.html

> Ver [`routes.md`](routes.md) para el mapa completo de rutas de la API.

## Estándar de codificación

[Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)

## Extensiones recomendadas (VSCode)

Ver `.vscode/extensions.json`.

---

## Observabilidad

El proyecto incluye un stack de observabilidad opcional (**VictoriaMetrics**, **vmagent**, **Grafana**) administrado mediante Docker Compose Profiles. Los servicios de observabilidad **no se levantan por defecto** para no afectar el rendimiento del sistema en la fase de desarrollo.

### Ejecución con/sin observabilidad

```bash
# Sin observabilidad (flujo normal)
docker compose up -d --build

# Con observabilidad
docker compose --profile observability up -d --build
```

### Detener servicios

```bash
# Sin perfil activo
docker compose down

# Si se levantó con perfil (evita que la red quede retenida)
docker compose --profile observability down

# Bajar todos los perfiles y eliminar volúmenes
docker compose --profile "*" down -v
```

### URLs

| Servicio            | URL                                       | Descripción                |
| ------------------- | ----------------------------------------- | -------------------------- |
| Grafana             | http://localhost:3000                     | Dashboard de métricas      |
| VictoriaMetrics     | http://localhost:8428                     | Almacenamiento de métricas |
| RabbitMQ Admin      | http://localhost:15672                    | Consola RabbitMQ           |
| Gateway Health      | http://localhost:8080/actuator/health     | Estado del gateway         |
| Gateway Prometheus  | http://localhost:8080/actuator/prometheus | Métricas del gateway       |
| Catálogo Prometheus | http://localhost:8081/actuator/prometheus | Métricas de catálogo       |
| Órdenes Prometheus  | http://localhost:8082/actuator/prometheus | Métricas de órdenes        |
| Ventas Prometheus   | http://localhost:8083/actuator/prometheus | Métricas de ventas         |

### Acceso a Grafana

- **URL:** `http://localhost:3000`
- **Usuario:** `admin` / **Password:** `admin`

> Si el provisioning está montado correctamente, Grafana cargará automáticamente el Data Source `VictoriaMetrics` y el dashboard de Spring Boot al arrancar.

### Notas

- `vmagent` recolecta métricas desde `/actuator/prometheus` de cada servicio y las envía a VictoriaMetrics.
- Grafana consulta las métricas directamente desde VictoriaMetrics.
- Si un panel muestra `No data` recién arrancado, es normal: aún no hay suficientes muestras para calcular tasas (`rate(...)`).
