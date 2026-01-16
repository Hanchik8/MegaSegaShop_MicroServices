FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml .
COPY .mvn .mvn
COPY mvnw mvnw
COPY mvnw.cmd mvnw.cmd
COPY discovery-server/pom.xml discovery-server/pom.xml
RUN chmod +x mvnw && ./mvnw -q -f discovery-server/pom.xml -DskipTests dependency:go-offline

COPY discovery-server/src discovery-server/src
RUN ./mvnw -q -f discovery-server/pom.xml -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/discovery-server/target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app/app.jar"]

