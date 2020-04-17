# Cloud Run Markdown Preview Sample

[Securing Cloud Run services tutorial](https://cloud.google.com/run/docs/tutorials/secure-services) walks through how to create a secure two-service application running on Cloud Run. This application is a Markdown editor which includes a public "frontend" service which anyone can use to compose markdown text, and a private "backend" service which renders Markdown text to HTML.

For more details on how to work with this sample read the [Google Cloud Run Java Samples README](https://github.com/GoogleCloudPlatform/java-docs-samples/tree/master/run).

[![Run in Google Cloud][run_img]][run_link]

## Dependencies

* **Spring Boot**: Web server framework.
* **Commonmark**: Java library for parsing and rendering Markdown text.
* **owasp-java-html-sanitizer**: Java library for sanitizing HTML.
* **Thymeleaf** Java template engine.
* **Jib**: Container build tool.

## Build the Container Image

1. Update `PROJECT_ID` in both `pom.xml` files with your GCP Project Id:

  ```
  <plugin>
    <groupId>com.google.cloud.tools</groupId>
    <artifactId>jib-maven-plugin</artifactId>
    <version>2.1.0</version>
    <configuration>
      <to>
        <image>gcr.io/PROJECT_ID/renderer</image>
      </to>
    </configuration>
  </plugin>
  ```
1. Use the `Jib Maven Plugin` to build and push your image to the Google
  Container Registry:

  ```
  mvn compile jib:build
  ```

## Environment Variables

Cloud Run services can be [configured with Environment Variables](https://cloud.google.com/run/docs/configuring/environment-variables).
Required variables for this sample include:

* `EDITOR_UPSTREAM_RENDER_URL`: The URL of the restricted Cloud Run service that
  renders Markdown to HTML.

[run_img]: https://storage.googleapis.com/cloudrun/button.svg
[run_link]: https://deploy.cloud.run/?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&dir=run/markdown-preview
