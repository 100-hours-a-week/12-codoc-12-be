# syntax=docker/dockerfile:1

FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app

# Copy Gradle wrapper & build scripts (layer cache for dependency resolution)
COPY gradlew settings.gradle* build.gradle* ./
COPY gradle/ gradle/
RUN chmod +x ./gradlew

# Build — cache Gradle home between builds to skip re-downloading deps
COPY . .
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew bootJar --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]