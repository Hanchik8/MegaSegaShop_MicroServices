FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml .
COPY .mvn .mvn
COPY mvnw mvnw
COPY mvnw.cmd mvnw.cmd
COPY api-gateway/pom.xml api-gateway/pom.xml
RUN chmod +x mvnw && ./mvnw -q -f api-gateway/pom.xml -DskipTests dependency:go-offline

COPY api-gateway/src api-gateway/src
RUN ./mvnw -q -f api-gateway/pom.xml -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/api-gateway/target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app/app.jar"]

