# Stage 1 — Build
FROM maven:3.9.11-eclipse-temurin-21 AS builder
WORKDIR /app

# 1. Copy wrapper and pom first to cache dependencies perfectly
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline -B -q

# 2. Copy source and build
COPY src ./src
RUN ./mvnw clean package -DskipTests -B -q


# Stage 2 — Run
FROM amazoncorretto:21-alpine
WORKDIR /app

#3. Create appuser and appgroup to run the application with least privilege & create logs directory
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

RUN mkdir -p /app/logs && chown -R appuser:appgroup /app /app/logs

# 4. Copy directly with ownership to avoid extra layer weights
COPY --from=builder --chown=appuser:appgroup /app/target/*.jar app.jar

USER appuser

# 5. Optimized JVM flags for modern containers
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError"

EXPOSE 8080

# 6. Better executive form without 'sh -c' to pass OS signals correctly
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
