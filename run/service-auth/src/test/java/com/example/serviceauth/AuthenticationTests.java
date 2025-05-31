/*
 * Copyright 2025 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.serviceauth;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.api.client.http.HttpStatusCodes;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.IdTokenCredentials;
import com.google.auth.oauth2.IdTokenProvider;
import com.google.auth.oauth2.IdTokenProvider.Option;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class AuthenticationTests {

  private static String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static String REGION = "us-central1";
  private String projectNumber;
  private String serviceUrl;
  private String serviceName;
  private HttpClient httpClient;

  @BeforeEach
  public void setUp() {
    this.projectNumber = getProjectNumber();
    this.serviceName = generateServiceName();
    this.serviceUrl = generateServiceUrl();
    this.deployService();

    this.httpClient = HttpClient.newHttpClient();
  }

  @AfterEach
  public void tearDown() {
    this.deleteService();
  }

  private String getProjectNumber() {
    return getOutputFromCommand(
        List.of("gcloud", "projects", "describe", PROJECT_ID, "--format=value(projectNumber)"));
  }

  private String generateServiceName() {
    return String.format("receive-java-%s", UUID.randomUUID().toString().substring(0, 8));
  }

  private String generateServiceUrl() {
    return String.format("https://%s-%s.%s.run.app", this.serviceName, this.projectNumber, REGION);
  }

  private String deployService() {
    return getOutputFromCommand(
        List.of(
            "gcloud",
            "run",
            "deploy",
            serviceName,
            "--project",
            PROJECT_ID,
            "--source",
            ".",
            "--region=" + REGION,
            "--allow-unauthenticated",
            "--set-env-vars=SERVICE_URL=" + serviceUrl,
            "--quiet"));
  }

  private String deleteService() {
    return getOutputFromCommand(
        List.of(
            "gcloud",
            "run",
            "services",
            "delete",
            serviceName,
            "--project",
            PROJECT_ID,
            "--async",
            "--region=" + REGION,
            "--quiet"));
  }

  private String getOutputFromCommand(List<String> command) {
    try {
      ProcessBuilder processBuilder = new ProcessBuilder(command);

      Process process = processBuilder.start();
      String output =
          new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8).strip();

      process.waitFor();

      return output;
    } catch (InterruptedException | IOException exception) {
      return String.format("Exception: %s", exception);
    }
  }

  private String getGoogleIdToken() {
    try {
      GoogleCredentials googleCredentials = GoogleCredentials.getApplicationDefault();

      IdTokenCredentials idTokenCredentials =
          IdTokenCredentials.newBuilder()
              .setIdTokenProvider((IdTokenProvider) googleCredentials)
              .setTargetAudience(serviceUrl)
              .setOptions(Arrays.asList(Option.FORMAT_FULL, Option.LICENSES_TRUE))
              .build();

      return idTokenCredentials.refreshAccessToken().getTokenValue();
    } catch (IOException exception) {
      return "error_generating_token";
    }
  }

  private HttpResponse<String> executeRequest(String headerName, String headerValue) {
    HttpRequest.Builder requestBuilder = HttpRequest.newBuilder().uri(URI.create(serviceUrl)).GET();
    if (headerName != null) {
      requestBuilder = requestBuilder.header(headerName, headerValue);
    }
    HttpRequest request = requestBuilder.build();
    HttpResponse<String> response = null;
    int retryDelay = 2000;
    int retryLimit = 3;

    for (int attempt = 0; attempt < retryLimit; attempt++) {
      try {
        response = this.httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == HttpStatusCodes.STATUS_CODE_OK
            || response.statusCode() == HttpStatusCodes.STATUS_CODE_UNAUTHORIZED) {
          return response;
        }
      } catch (HttpTimeoutException exception) {
        System.out.println(String.format("TimeoutException: %s", exception));
        System.out.println("Retrying...");
      } catch (IOException | InterruptedException exception) {
        System.out.println(String.format("Exception: %s", exception));
        System.out.println("Retrying...");
      }

      try {
        Thread.sleep(retryDelay);
      } catch (InterruptedException exception) {
        Thread.currentThread().interrupt();
        return null;
      }
    }

    return null;
  }

  @Test
  public void testValidToken() throws Exception {
    String token = getGoogleIdToken();
    HttpResponse<String> response = executeRequest("X-Serverless-Authorization", "Bearer " + token);

    assertTrue(response != null);
    assertTrue(response.statusCode() == HttpStatusCodes.STATUS_CODE_OK);
    assertTrue(response.body().contains("Hello,"));
  }

  @Test
  public void testInvalidToken() throws Exception {
    String token = "invalid_token";
    HttpResponse<String> response = executeRequest("X-Serverless-Authorization", "Bearer " + token);

    assertTrue(response != null);
    assertTrue(response.statusCode() == HttpStatusCodes.STATUS_CODE_UNAUTHORIZED);
    assertTrue(response.body().contains("Please supply a valid bearer token."));
  }

  @Test
  public void testAnonymousRequest() throws Exception {
    HttpResponse<String> response = executeRequest(null, null);

    assertTrue(response != null);
    assertTrue(response.statusCode() == HttpStatusCodes.STATUS_CODE_UNAUTHORIZED);
    assertTrue(response.body().contains("missing X-Serverless-Authorization header"));
  }
}
