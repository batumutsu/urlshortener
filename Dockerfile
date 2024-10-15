FROM openjdk:21-jdk-slim as builder

WORKDIR /app

# Pass build arguments as environment variables
ARG SPRING_TEST_PORT
ARG APP_CONTEXT_PATH
ARG LOCAL_HOST
ARG KEY_ALPHABETS
ARG KEY_LENGTH

# Set environment variables for Maven test run
ENV SPRING_TEST_PORT=$SPRING_TEST_PORT \
    APP_CONTEXT_PATH=$APP_CONTEXT_PATH \
    LOCAL_HOST=$LOCAL_HOST \
    KEY_ALPHABETS=$KEY_ALPHABETS \
    KEY_LENGTH=$KEY_LENGTH

# Copy the Maven wrapper and pom.xml file
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Download dependencies
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src src

# Build the application
RUN ./mvnw package

FROM openjdk:21-jdk-slim

WORKDIR /app

COPY --from=builder /app/target/urlshortener-0.0.1-SNAPSHOT.jar urlshortener_app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "urlshortener_app.jar"]