FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml .
COPY .mvn .mvn
COPY mvnw mvnw
COPY mvnw.cmd mvnw.cmd
COPY product-service/pom.xml product-service/pom.xml
RUN chmod +x mvnw && ./mvnw -q -pl product-service -am -DskipTests package

COPY product-service/src product-service/src
RUN ./mvnw -q -pl product-service -am -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/product-service/target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app/app.jar"]
