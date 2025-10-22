FROM maven:3.9.9-eclipse-temurin-17 AS builder
WORKDIR /build

COPY pom.xml .

RUN mvn -B -q dependency:go-offline
COPY src ./src

RUN mvn -B clean package
#RUN mvn -B clean package -DskipTests

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

ARG JAR_FILE=/build/target/*.jar
COPY --from=builder ${JAR_FILE} app.jar

ENV SPRING_PROFILES_ACTIVE=dev
ENV SERVER_PORT=8901

EXPOSE 8901

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
