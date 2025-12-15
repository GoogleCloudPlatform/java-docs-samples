# Build the application
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /app

# Copy only the build config first (for better layer caching)
COPY pom.xml .
COPY src ./src

# 'clean package': Compiles the code and creates the thin jar in /app/target
# 'dependency:copy-dependencies': Copies all JARs to /app/target/libs
# We explicitly set -DoutputDirectory so we know EXACTLY where they are.
RUN mvn clean package dependency:copy-dependencies \
    -DoutputDirectory=target/libs \
    -DskipTests

# Run the application
FROM eclipse-temurin:17-jre-focal

# Security: Create a non-root user
RUN useradd -m appuser
USER appuser
WORKDIR /app

# Copy the Thin Jar (Your application code)
# Ensure the name 'auth-1.0.jar' matches your pom.xml artifactId/version
COPY --from=builder --chown=appuser:appuser /app/target/auth-1.0.jar app.jar

# Copy the Dependencies (The libraries)
COPY --from=builder --chown=appuser:appuser /app/target/libs lib/

# Run with Classpath
# We add 'app.jar' and everything in 'lib/' to the classpath.
# We explicitly specify the Main Class here, so your shared pom.xml doesn't need to change.
CMD ["java", "-cp", "app.jar:lib/*", "com.google.cloud.auth.samples.customcredentials.aws.CustomCredentialSupplierAwsWorkload"]