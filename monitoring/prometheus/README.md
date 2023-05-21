# Prometheus Sample for Java - SLIs

This section contains a sample of using [Prometheus](https://prometheus.io) to instrument a Spring Boot web application to emit Service Level Indicator metrics.

[![Run in Google Cloud][run_img]][run_link]

[run_img]: https://storage.googleapis.com/cloudrun/button.svg
[run_link]: https://deploy.cloud.run/?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&dir=monitoring/prometheus

1. Build and run locally

        mvn spring-boot:run

2. Visit `http://localhost:8080` to view your application.

3. Visit `http://localhost:8080/metrics` to view your metrics.

## Dependencies

* **Spring Boot**: Web server framework.
* **Prometheus JVM Client**: Prometheus instrumentation library for JVM applications.
* **Junit**: [development] Test running framework.
