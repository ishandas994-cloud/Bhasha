# ---- Stage 1: build the Spring Boot jar with Maven ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy pom first and resolve dependencies separately so Docker can cache
# them between builds - source changes don't re-download the internet.
COPY pom.xml .
RUN mvn dependency:go-offline -B -q

COPY src ./src
RUN mvn package -DskipTests -B -q

# ---- Stage 2: slim runtime image ----
FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /app/target/bangla-lang-backend.jar app.jar

# Render/Railway/Cloud Run inject PORT; Spring reads server.port from it.
ENV PORT=8081
EXPOSE 8081

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
