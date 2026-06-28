# --- Etapa del agente OpenTelemetry ---
# Descarga el agente Java de OpenTelemetry en una version fija y valida su checksum
FROM alpine:3.22 AS otel-agent

ARG OTEL_AGENT_VERSION=2.28.1
ARG OTEL_AGENT_SHA256=faa89bdeebf9b1f52be4a4374689176717b02a59df2d8f8b6eb9aa39f9292589

RUN apk add --no-cache curl
RUN curl --fail --silent --show-error --location \
    "https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v${OTEL_AGENT_VERSION}/opentelemetry-javaagent.jar" \
    --output /tmp/opentelemetry-javaagent.jar \
    && echo "${OTEL_AGENT_SHA256}  /tmp/opentelemetry-javaagent.jar" | sha256sum -c -

# --- Etapa de dependencias ---
# Descarga todas las dependencias y plugins sin compilar
# Lo usamos como base en dev
FROM maven:3.9-eclipse-temurin-17 AS dependencies
WORKDIR /app
COPY pom.xml .
COPY spotbugs-exclude.xml .
COPY lombok.config .
RUN mvn dependency:go-offline

# --- Etapa de desarrollo ---
# Agrega el código fuente sin correr tests ni validaciones
# Lo usamos en docker compose watch para dev
FROM dependencies AS dev
COPY src ./src

# --- Etapa de build ---
# Por defecto corre tests + SpotBugs + JaCoCo. En CI se puede pasar
# MAVEN_ARGS="package -DskipTests" porque los tests ya corrieron en un step previo.
FROM dev AS build
ARG MAVEN_ARGS=verify
RUN mvn $MAVEN_ARGS

# --- Etapa de runtime ---
# Se usa JRE Alpine para ejecutar — imagen mas liviana que la estandar
# forzamos la plataforma linux/amd64 porque eclipse-temurin:alpine no tiene soporte ARM64
FROM --platform=linux/amd64 eclipse-temurin:17-jre-alpine
WORKDIR /app

# Creamos un usuario sin privilegios para correr la app
# Si alguien explota la app, no tiene acceso root al contenedor
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copiamos el JAR
COPY --from=build /app/target/*.jar app.jar

# Copiamos el agente de OpenTelemetry validado en la etapa otel-agent
COPY --from=otel-agent /tmp/opentelemetry-javaagent.jar /app/opentelemetry-javaagent.jar

# Configuracion base de OpenTelemetry. El endpoint OTLP, las credenciales y
# OTEL_RESOURCE_ATTRIBUTES se configuran como variables de entorno en Render.
ENV OTEL_SERVICE_NAME=tacs-backend
ENV OTEL_EXPORTER_OTLP_PROTOCOL=http/protobuf
ENV OTEL_TRACES_EXPORTER=otlp
ENV OTEL_METRICS_EXPORTER=otlp
ENV OTEL_LOGS_EXPORTER=none
ENV OTEL_TRACES_SAMPLER=parentbased_always_on

# Temporalidad cumulative: Grafana Cloud (Mimir) es Prometheus por debajo y rechaza con
# HTTP 400 ("invalid temporality and type combination") varias metricas en delta. Esto se
# verifico en dev con el OtlpMeterRegistry de Micrometer: en delta fallaba cada push, en
# cumulative ingesta sin errores. Cumulative es el modelo nativo de Mimir.
# Histogramas exponenciales (native histograms): mejores percentiles con muchas menos series
# que el clasico (menor presion sobre el limite de cardinalidad del free tier). Si Grafana
# Cloud rechazara los native histograms en prod, quitar la linea de abajo para volver al
# explicit-bucket clasico. Validar percentiles en staging antes de prod.
ENV OTEL_EXPORTER_OTLP_METRICS_TEMPORALITY_PREFERENCE=cumulative
ENV OTEL_EXPORTER_OTLP_METRICS_DEFAULT_HISTOGRAM_AGGREGATION=base2_exponential_bucket_histogram

# Cambiamos al usuario sin privilegios
USER appuser

# Exponemos el puerto
EXPOSE 8080

# Ejecutamos la app con el agente de OpenTelemetry
ENTRYPOINT ["java", "-javaagent:/app/opentelemetry-javaagent.jar", "-jar", "app.jar"]
