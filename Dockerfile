# --- Stage 1: Build the Frontend ---
FROM node:20-alpine AS frontend-builder
WORKDIR /frontend
COPY frontend/package*.json ./
RUN npm install
COPY frontend/ .
RUN npm run build

# --- Stage 2: Build the Backend ---
FROM maven:3.9.6-eclipse-temurin-17 AS backend-builder
WORKDIR /build

# Copy backend pom.xml to cache dependencies
COPY backend/pom.xml .
RUN mvn dependency:go-offline -B

# Copy backend source code
COPY backend/src ./src

# Copy compiled frontend assets into the Spring Boot static resources directory
COPY --from=frontend-builder /frontend/dist/ ./src/main/resources/static/

# Package the backend application (including the copied static resources)
RUN mvn clean package -DskipTests

# --- Stage 3: Runtime Stage ---
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copy the built fat JAR file from the builder stage
COPY --from=backend-builder /build/target/Gmart_Full_Stack-1.0-SNAPSHOT.jar app.jar

# Expose port 8080 for web access
EXPOSE 8080

# Execute the application
ENTRYPOINT [ "java", "-jar", "app.jar" ]
