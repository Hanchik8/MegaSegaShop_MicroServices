FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml .
COPY .mvn .mvn
COPY mvnw mvnw
COPY mvnw.cmd mvnw.cmd
COPY user-service/pom.xml user-service/pom.xml
RUN chmod +x mvnw && ./mvnw -q -f user-service/pom.xml -DskipTests dependency:go-offline

COPY user-service/src user-service/src
RUN ./mvnw -q -f user-service/pom.xml -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/user-service/target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app/app.jar"]

