# Ejecución del microservicio principal (sin docker)

1. Deben estar activos los contenedores de infraestructura (MySQL y RabbitMQ):

```bash
docker compose up mysql rabbitmq -d
```

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=mysql -DskipTests
# o
mvn spring-boot:run -Dspring-boot.run.profiles=mysql -DskipTests
```
