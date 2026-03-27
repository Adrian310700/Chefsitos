# Etapa 1: Construcción
ARG SHARED_BUILDER_IMAGE=local/uamishop-shared-builder:latest
FROM ${SHARED_BUILDER_IMAGE} AS builder

WORKDIR /build

# Compilar microservicio
WORKDIR /build/uamishop-ordenes
COPY uamishop-ordenes/pom.xml ./pom.xml
RUN mvn dependency:go-offline -B
COPY uamishop-ordenes/src ./src
RUN mvn clean package -DskipTests -B

# Etapa 2: Ejecución
FROM eclipse-temurin:21-jre

WORKDIR /app

# Usuario no root para seguridad
RUN groupadd -g 1001 appgroup && useradd -u 1001 -g appgroup -m appuser
USER appuser

# Copiar el JAR generado desde el stage de build
COPY --from=builder /build/uamishop-ordenes/target/*.jar app.jar

# Puerto del microservicio de órdenes
EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]
