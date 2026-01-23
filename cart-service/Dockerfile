FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml .
COPY .mvn .mvn
COPY mvnw mvnw
COPY mvnw.cmd mvnw.cmd

# Copy shared modules
COPY .shared .shared

COPY cart-service/pom.xml cart-service/pom.xml

# Build shared modules first, then resolve dependencies
RUN chmod +x mvnw && \
    ./mvnw -q -pl .shared/shared-security,.shared/shared-web,.shared/shared-dto install -DskipTests && \
    ./mvnw -q -f cart-service/pom.xml -DskipTests dependency:go-offline

COPY cart-service/src cart-service/src
RUN ./mvnw -q -f cart-service/pom.xml -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/cart-service/target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app/app.jar"]
