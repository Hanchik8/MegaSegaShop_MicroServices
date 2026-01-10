FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml .
COPY .mvn .mvn
COPY mvnw mvnw
COPY mvnw.cmd mvnw.cmd
COPY auth-service/pom.xml auth-service/pom.xml
RUN chmod +x mvnw && ./mvnw -q -pl auth-service -am -DskipTests package

COPY auth-service/src auth-service/src
RUN ./mvnw -q -pl auth-service -am -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/auth-service/target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app/app.jar"]
