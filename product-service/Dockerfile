FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml .
COPY .mvn .mvn
COPY mvnw mvnw
COPY mvnw.cmd mvnw.cmd

# Copy shared modules
COPY .shared .shared

COPY product-service/pom.xml product-service/pom.xml

# Build shared modules first, then resolve dependencies
RUN chmod +x mvnw && \
    ./mvnw -q -N -f pom.xml install && \
    ./mvnw -q -f .shared/shared-security/pom.xml -DskipTests install && \
    ./mvnw -q -f .shared/shared-web/pom.xml -DskipTests install && \
    ./mvnw -q -f .shared/shared-dto/pom.xml -DskipTests install && \
    ./mvnw -q -f product-service/pom.xml -DskipTests dependency:go-offline

COPY product-service/src product-service/src
RUN ./mvnw -q -f product-service/pom.xml -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/product-service/target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app/app.jar"]
