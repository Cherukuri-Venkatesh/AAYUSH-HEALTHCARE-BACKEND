# Stage 1: Build the WAR

FROM maven:3.9.11-eclipse-temurin-21 AS build

WORKDIR /app

# Copy pom.xml and download dependencies (cached)

COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy project source

COPY src ./src

# Build WAR file

RUN mvn clean package -DskipTests


# Stage 2: Run the WAR

FROM eclipse-temurin:21-jre-ubi9-minimal

WORKDIR /app

# Copy WAR from build stage

COPY --from=build /app/target/*.war app.war

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.war"]