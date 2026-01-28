FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml .
COPY .mvn .mvn
COPY mvnw mvnw
COPY mvnw.cmd mvnw.cmd
COPY notification-service/pom.xml notification-service/pom.xml
RUN chmod +x mvnw && ./mvnw -q -f notification-service/pom.xml -DskipTests dependency:go-offline

COPY notification-service/src notification-service/src
RUN ./mvnw -q -f notification-service/pom.xml -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/notification-service/target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app/app.jar"]

