# Ejecución del microservicio de ventas (sin docker)

1. Deben estar activos los contenedores de infraestructura (MySQL y RabbitMQ):

```bash
docker compose up mysql rabbitmq -d
```

```bash
./mvnw spring-boot:run -DskipTests
# o
mvn spring-boot:run -DskipTests
```
