# ---------- Build stage ----------
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

# Copy Maven wrapper and pom first (for better caching)
COPY mvnw pom.xml ./
COPY .mvn .mvn

# Ensure wrapper is executable
RUN chmod +x mvnw

# Download dependencies
RUN ./mvnw dependency:go-offline

# Copy the rest of the source
COPY src src

# Build the Spring Boot executable JAR
RUN ./mvnw -B -DskipTests clean package

# ---------- Runtime stage ----------
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copy only the repackaged Spring Boot JAR
COPY --from=build /app/target/user-portal.jar app.jar

# JVM options
ENV JAVA_OPTS="-Xms256m -Xmx512m"

EXPOSE 8080

# Use exec form so signals are forwarded correctly
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]