# Paso 1: compilar libreria compartida
FROM maven:3.9-eclipse-temurin-21 AS shared-builder
WORKDIR /build
COPY uamishop-shared/pom.xml uamishop-shared/
COPY uamishop-shared/src uamishop-shared/src
RUN mvn -f uamishop-shared/pom.xml clean install -DskipTests
# -f para indicar el archivo pom.xml especifico, sin estar dentro de la carpeta
# Se guarda la libreria compilada en el repositorio local de Maven dentro del contenedor

# Paso 2: Base para microservicios, con la libreria ya instalada
FROM shared-builder AS microservice-builder
ARG SERVICE_NAME
WORKDIR /build/${SERVICE_NAME}
COPY ${SERVICE_NAME}/pom.xml .
# Esto descargará dependencias externas, la 'shared' ya está en el repo local
RUN mvn dependency:go-offline -B
COPY ${SERVICE_NAME}/src ./src
RUN mvn clean package -DskipTests -B

# Paso 3: Creacion de la imagen final para cada microservicio
FROM eclipse-temurin:21-jre AS final
WORKDIR /app
ARG SERVICE_NAME
ARG SERVICE_PORT
# Copiar desde el builder usando la ruta dinámica
COPY --from=microservice-builder /build/${SERVICE_NAME}/target/*.jar app.jar

RUN groupadd -g 1001 appgroup && useradd -u 1001 -g appgroup -m appuser
USER appuser

EXPOSE ${SERVICE_PORT}
ENTRYPOINT ["java", "-jar", "app.jar"]
