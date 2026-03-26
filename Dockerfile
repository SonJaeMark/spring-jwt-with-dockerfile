FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
COPY mvnw.cmd .

RUN chmod +x mvnw && ./mvnw -q -DskipTests dependency:go-offline

COPY src src

RUN ./mvnw -q -DskipTests clean package

FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

RUN addgroup --system spring && adduser --system --ingroup spring spring

COPY --from=build /app/target/*.jar app.jar

USER spring

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
