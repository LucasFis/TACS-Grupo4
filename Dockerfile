# --- Etapa de build ---
# Usa Maven con JDK 17 para compilar el proyecto
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copiamos el pom, la configuración de SpotBugs, la configuracion de lombok y el código fuente
COPY pom.xml .
COPY spotbugs-exclude.xml .
COPY lombok.config .
COPY src ./src

# Compilamos, corremos tests y validaciones, y generamos el JAR
RUN mvn verify

# --- Etapa de runtime ---
# Se usa JRE para ejecutar
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copiamos el JAR
COPY --from=build /app/target/*.jar app.jar

# Exponemos el puerto
EXPOSE 8080

# Ejecutamos la app
ENTRYPOINT ["java", "-jar", "app.jar"]