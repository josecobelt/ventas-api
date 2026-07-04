# ---------- Etapa de build ----------
FROM maven:3.9.6-eclipse-temurin-8 AS build
WORKDIR /app

# Se copia primero el pom.xml para aprovechar el cache de capas de Docker
# con las dependencias, y solo se vuelve a descargar todo si el pom cambia.
COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B clean package -DskipTests

# ---------- Etapa de ejecución ----------
FROM eclipse-temurin:8-jre-alpine
WORKDIR /app

COPY --from=build /app/target/ventas-api-*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
