# API Gateway - UAMIShop

API Gateway que centraliza y redirige las peticiones HTTP a los microservicios internos de UAMIShop.

## Ejecución con Docker (predeterminada)

El gateway se comunica con los microservicios usando la red interna de Docker.

```bash
# Desde la raíz del proyecto
docker compose up -d
```

> Las rutas en `application.yml` usan los nombres de servicio de Docker (`http://uamishop-catalogo:8081`), por lo que no es necesario ningún cambio.

## Ejecución local (`local`)

Para ejecutar el gateway directamente en el host, usar el perfil `local`. Este perfil reemplaza las URIs internas de Docker por `localhost`.

### Requisitos previos

Levantar la infraestructura y los microservicios. Pueden estar en Docker con puertos expuestos o ejecutándose localmente:

```bash
# Opción A: Microservicios en Docker con puertos expuestos
docker compose up mysql rabbitmq uamishop-catalogo uamishop-ordenes uamishop-ventas -d

# Opción B: Microservicios locales (cada uno en su terminal)
cd uamishop-catalogo && ./mvnw spring-boot:run -DskipTests
cd uamishop-ordenes  && ./mvnw spring-boot:run -DskipTests
cd uamishop-ventas   && ./mvnw spring-boot:run -DskipTests
```

### Ejecutar el gateway

```bash
cd uamishop-gateway
./mvnw spring-boot:run -DskipTests -Dspring-boot.run.profiles=local
```

### Verificar

```bash
# Debe devolver la respuesta del microservicio de catálogo
curl http://localhost:8080/api/v1/productos
```

## Endpoints de Actuator

| Endpoint                 | Descripción                                           |
| ------------------------ | ----------------------------------------------------- |
| `GET /actuator/health`   | Estado de salud del gateway                           |
| `GET /actuator/info`     | Información de la aplicación                          |
| `GET /actuator/mappings` | Todas las rutas registradas (incluye las del gateway) |

## Rutas configuradas

| Ruta                    | Microservicio destino    |
| ----------------------- | ------------------------ |
| `/api/v1/productos/**`  | `uamishop-catalogo:8081` |
| `/api/v1/categorias/**` | `uamishop-catalogo:8081` |
| `/api/v1/ordenes/**`    | `uamishop-ordenes:8082`  |
| `/api/v1/carritos/**`   | `uamishop-ventas:8083`   |
