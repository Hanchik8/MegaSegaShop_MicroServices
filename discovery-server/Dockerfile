FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml .
COPY .mvn .mvn
COPY mvnw mvnw
COPY mvnw.cmd mvnw.cmd
COPY discovery-server/pom.xml discovery-server/pom.xml
RUN chmod +x mvnw && ./mvnw -q -pl discovery-server -am -DskipTests package

COPY discovery-server/src discovery-server/src
RUN ./mvnw -q -pl discovery-server -am -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/discovery-server/target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app/app.jar"]
