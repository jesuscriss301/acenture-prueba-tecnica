# ---------- Etapa 1: compilar, probar y empaquetar ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Primero solo el pom: las dependencias quedan en caché mientras no cambie
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
# Las pruebas unitarias se ejecutan durante la construcción de la imagen
RUN mvn -B package

# ---------- Etapa 2: imagen final liviana ----------
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Usuario sin privilegios
RUN addgroup -S app && adduser -S app -G app

COPY --from=build /app/target/*.jar app.jar
USER app

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
