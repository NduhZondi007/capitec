## Multi-stage build for Transaction API

# Stage 1: build the application using Maven
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -q clean package -DskipTests

# Stage 2: create a lightweight runtime image
FROM eclipse-temurin:17-jre-jammy AS runtime
WORKDIR /app
COPY --from=build /app/target/transaction-api-*.jar app.jar
EXPOSE 8080
# Non-root user for security
RUN addgroup --system api && adduser --system --no-create-home --ingroup api api
USER api
ENTRYPOINT ["java","-jar","/app/app.jar"]