# Multi-stage build using Maven to package the application
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /build

# Copy the pom.xml first to download and cache Maven dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy the source code and build the package
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage using Eclipse Temurin JRE 17
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copy the built JAR file from the builder stage
COPY --from=builder /build/target/Gmart_Full_Stack-1.0-SNAPSHOT.jar app.jar

# Expose port 8080 for web access
EXPOSE 8080

# Execute the application
ENTRYPOINT [ "java", "-jar", "app.jar" ]
