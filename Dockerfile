# syntax=docker/dockerfile:1

FROM eclipse-temurin:26-jdk AS build
WORKDIR /workspace
COPY gradlew settings.gradle ./
COPY gradle gradle
COPY api api
RUN ./gradlew :api:bootJar --no-daemon

FROM eclipse-temurin:26-jre
WORKDIR /app
COPY --from=build /workspace/api/build/libs/api.jar doodle-api.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "doodle-api.jar"]
