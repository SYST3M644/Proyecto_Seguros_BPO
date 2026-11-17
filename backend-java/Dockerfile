FROM maven:3.9.11-eclipse-temurin-21-alpine AS build
WORKDIR /workspace
COPY pom.xml ./
COPY catalogos-service/pom.xml catalogos-service/pom.xml
COPY siniestros-service/pom.xml siniestros-service/pom.xml
COPY api-gateway/pom.xml api-gateway/pom.xml
RUN mvn -B -ntp -DskipTests dependency:go-offline
COPY catalogos-service/src catalogos-service/src
COPY siniestros-service/src siniestros-service/src
COPY api-gateway/src api-gateway/src
RUN mvn -B -ntp -DskipTests package

FROM eclipse-temurin:21-jre-alpine
ARG MODULE
RUN test -n "$MODULE" && addgroup -S seguros && adduser -S -G seguros seguros \
    && mkdir -p /app/datos && chown -R seguros:seguros /app
WORKDIR /app
COPY --from=build --chown=seguros:seguros /workspace/${MODULE}/target/${MODULE}-1.0.0-SNAPSHOT.jar /app/app.jar
USER seguros
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
