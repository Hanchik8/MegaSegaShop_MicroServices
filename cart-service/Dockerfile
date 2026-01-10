FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml .
COPY .mvn .mvn
COPY mvnw mvnw
COPY mvnw.cmd mvnw.cmd
COPY cart-service/pom.xml cart-service/pom.xml
RUN chmod +x mvnw && ./mvnw -q -pl cart-service -am -DskipTests package

COPY cart-service/src cart-service/src
RUN ./mvnw -q -pl cart-service -am -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/cart-service/target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app/app.jar"]
