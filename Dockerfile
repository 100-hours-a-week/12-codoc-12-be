# syntax=docker/dockerfile:1

FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app

# Cache Gradle dependencies
COPY gradlew settings.gradle* build.gradle* gradle/ ./
RUN chmod +x ./gradlew
RUN ./gradlew --no-daemon dependencies || true

# Build
COPY . .
RUN ./gradlew clean bootJar --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
