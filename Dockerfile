# Stage 1 — Download dependencies
FROM eclipse-temurin:21-jdk-alpine AS dependencies

RUN apk add --no-cache maven

WORKDIR /build

COPY pom.xml .

RUN mvn dependency:go-offline


# Stage 2 — Build the app
FROM dependencies AS builder

COPY src ./src

RUN mvn clean package -DskipTests


# Stage 3 — Run the app
FROM eclipse-temurin:21-jre-alpine AS runtime

WORKDIR /app

COPY --from=builder /build/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]