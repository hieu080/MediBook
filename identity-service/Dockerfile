# syntax=docker/dockerfile:1

FROM maven:3.9.9-eclipse-temurin-17 AS builder
WORKDIR /build

# Copy root and all module pom files so Maven can resolve the reactor structure
COPY pom.xml .
COPY share-kernel/pom.xml share-kernel/pom.xml
COPY api-gateway/pom.xml api-gateway/pom.xml
COPY identity-service/pom.xml identity-service/pom.xml
COPY patient-service/pom.xml patient-service/pom.xml
COPY doctor-schedule-service/pom.xml doctor-schedule-service/pom.xml
COPY appointment-service/pom.xml appointment-service/pom.xml
COPY payment-service/pom.xml payment-service/pom.xml
COPY queue-service/pom.xml queue-service/pom.xml
COPY notification-service/pom.xml notification-service/pom.xml
COPY reporting-service/pom.xml reporting-service/pom.xml
COPY audit-service/pom.xml audit-service/pom.xml
RUN mvn -q -pl share-kernel,identity-service -am -DskipTests dependency:go-offline

# Copy only sources needed for the identity-service build graph
COPY share-kernel/src share-kernel/src
COPY identity-service/src identity-service/src
RUN mvn -q -pl identity-service -am clean package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app

RUN useradd -r -u 1001 -g root appuser
USER 1001

COPY --from=builder /build/identity-service/target/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar app.jar"]
