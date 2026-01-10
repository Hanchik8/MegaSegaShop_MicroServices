FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml .
COPY .mvn .mvn
COPY mvnw mvnw
COPY mvnw.cmd mvnw.cmd
COPY config-server/pom.xml config-server/pom.xml
RUN chmod +x mvnw && ./mvnw -q -pl config-server -am -DskipTests package

COPY config-server/src config-server/src
RUN ./mvnw -q -pl config-server -am -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/config-server/target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app/app.jar"]
