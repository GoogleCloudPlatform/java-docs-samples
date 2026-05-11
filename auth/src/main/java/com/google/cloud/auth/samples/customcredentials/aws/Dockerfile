FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package dependency:copy-dependencies \
    -DoutputDirectory=target/libs \
    -DskipTests

FROM eclipse-temurin:17.0.18_8-jre

RUN useradd -m appuser
USER appuser
WORKDIR /app

COPY --from=builder --chown=appuser:appuser /app/target/auth-1.0.jar app.jar
COPY --from=builder --chown=appuser:appuser /app/target/libs lib/

CMD ["java", "-cp", "app.jar:lib/*", "com.google.cloud.auth.samples.customcredentials.aws.CustomCredentialSupplierAwsWorkload"]
