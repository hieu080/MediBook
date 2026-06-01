# syntax=docker/dockerfile:1

FROM maven:3.9.9-eclipse-temurin-17 AS builder
WORKDIR /app

# Cache dependencies
COPY .mvn .mvn
COPY mvnw .
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline

# Build app
COPY src src
RUN mvn -q clean package -DskipTests

# Runtime image
FROM eclipse-temurin:17-jre
WORKDIR /app

# Non-root user
RUN useradd -r -u 1001 -g root appuser
USER 1001

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8087

# Runtime options configurable from env
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar app.jar"]