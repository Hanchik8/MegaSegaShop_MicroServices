FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml .
COPY .mvn .mvn
COPY mvnw mvnw
COPY mvnw.cmd mvnw.cmd
COPY inventory-service/pom.xml inventory-service/pom.xml
RUN chmod +x mvnw && ./mvnw -q -pl inventory-service -am -DskipTests package

COPY inventory-service/src inventory-service/src
RUN ./mvnw -q -pl inventory-service -am -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/inventory-service/target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app/app.jar"]
