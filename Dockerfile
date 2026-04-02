# syntax=docker/dockerfile:1
# Configuracion para decirle a docker que utilice la version 1 de su sintaxis, lo que permite usar ARG en etapas anteriores

FROM maven:3.9-eclipse-temurin-21 AS shared-builder
WORKDIR /build

ARG SHARED_MODULE=uamishop-shared

# Paso 1: Libreria compartida
# 1.1 Copiar solo el pom.xml para descargar dependencias
COPY ${SHARED_MODULE}/pom.xml ${SHARED_MODULE}/pom.xml
RUN --mount=type=cache,target=/root/.m2/repository \
    mvn -f ${SHARED_MODULE}/pom.xml -B dependency:go-offline

# 1.2 Compilar la libreria compartida
COPY ${SHARED_MODULE}/src ${SHARED_MODULE}/src
RUN --mount=type=cache,target=/root/.m2/repository \
    mvn -f ${SHARED_MODULE}/pom.xml -B install -DskipTests
# -f para indicar el archivo pom.xml especifico, sin estar dentro de la carpeta
# Se guarda en el repositorio local de Maven dentro del contenedor
# --mount=type... para cachear las dependencias descargadas y acelerar compilaciones futuras

# Paso 2: Base para microservicios, con la libreria ya instalada
FROM shared-builder AS microservice-builder
WORKDIR /build

ARG SERVICE_NAME

# 2.1 pom de microservicio
COPY ${SERVICE_NAME}/pom.xml ${SERVICE_NAME}/pom.xml
RUN --mount=type=cache,target=/root/.m2/repository \
    mvn -f ${SERVICE_NAME}/pom.xml -B dependency:go-offline

# 2.2 Codigo del microservicio
COPY ${SERVICE_NAME}/src ${SERVICE_NAME}/src
RUN --mount=type=cache,target=/root/.m2/repository \
    mvn -f ${SERVICE_NAME}/pom.xml -B package -DskipTests

# Paso 3: Creacion de la imagen final para cada microservicio
FROM eclipse-temurin:21-jre AS final
WORKDIR /app

ARG SERVICE_NAME
ARG SERVICE_PORT
# Copiar desde el builder usando la ruta dinámica
COPY --from=microservice-builder /build/${SERVICE_NAME}/target/*.jar /app/app.jar

RUN groupadd -g 1001 appgroup && useradd -u 1001 -g appgroup -m appuser
USER appuser

EXPOSE ${SERVICE_PORT}
ENTRYPOINT ["java", "-jar", "app.jar"]
