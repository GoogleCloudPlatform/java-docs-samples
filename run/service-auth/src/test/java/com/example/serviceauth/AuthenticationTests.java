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
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class AuthenticationTests {

  private static String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static String REGION = "us-central1";
  private static String PROJECT_NUMBER;
  private static String SERVICE_URL;
  private static String SERVICE_NAME;
  private static HttpClient HTTP_CLIENT;

  @BeforeAll
  public static void setUp() throws InterruptedException, IOException {
    PROJECT_NUMBER = getProjectNumber();
    SERVICE_NAME = generateServiceName();
    SERVICE_URL = generateServiceUrl();
    deployService();

    HTTP_CLIENT = HttpClient.newHttpClient();
    waitForService();
  }

  @AfterAll
  public static void tearDown() throws InterruptedException, IOException {
    deleteService();
  }

  private static String getProjectNumber() throws InterruptedException, IOException {
    return getOutputFromCommand(
        List.of("gcloud", "projects", "describe", PROJECT_ID, "--format=value(projectNumber)"));
  }

  private static String generateServiceName() {
    return String.format("receive-java-%s", UUID.randomUUID().toString().substring(0, 8));
  }

  private static String generateServiceUrl() {
    return String.format("https://%s-%s.%s.run.app", SERVICE_NAME, PROJECT_NUMBER, REGION);
  }

  private static String deployService() throws InterruptedException, IOException {
    return getOutputFromCommand(
        List.of(
            "gcloud",
            "run",
            "deploy",
            SERVICE_NAME,
            "--project",
            PROJECT_ID,
            "--source",
            ".",
            "--region=" + REGION,
            "--allow-unauthenticated",
            "--set-env-vars=SERVICE_URL=" + SERVICE_URL,
            "--quiet"));
  }

  private static String deleteService() throws InterruptedException, IOException {
    return getOutputFromCommand(
        List.of(
            "gcloud",
            "run",
            "services",
            "delete",
            SERVICE_NAME,
            "--project",
            PROJECT_ID,
            "--async",
            "--region=" + REGION,
            "--quiet"));
  }

  private static String getOutputFromCommand(List<String> command)
      throws InterruptedException, IOException {

    ProcessBuilder processBuilder = new ProcessBuilder(command);

    Process process = processBuilder.start();
    String output =
        new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8).strip();

    process.waitFor();

    return output;
  }

  private static void waitForService() {
    HttpResponse<String> response = null;
    int waitingTimeInSeconds = 1;
    int retryTimeLimitInSeconds = 32;
    while (waitingTimeInSeconds <= retryTimeLimitInSeconds) {
      response = executeRequest(buildRequest(null, null));
      if (response != null) {
        break;
      }
      waitingTimeInSeconds *= 2;
      try {
        Thread.sleep(waitingTimeInSeconds * 1000);
      } catch (Exception e) {
        Thread.currentThread().interrupt();
      }
    }
  }

  private static HttpRequest buildRequest(String headerName, String headerValue) {
    HttpRequest.Builder requestBuilder =
        HttpRequest.newBuilder().uri(URI.create(SERVICE_URL)).GET();
    if (headerName != null) {
      requestBuilder = requestBuilder.header(headerName, headerValue);
    }
    return requestBuilder.build();
  }

  private static HttpResponse<String> executeRequest(HttpRequest request) {
    HttpResponse<String> response = null;
    int retryDelay = 3000;
    int retryLimit = 5;

    for (int attempt = 0; attempt < retryLimit; attempt++) {
      try {
        response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
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
      }
    }

    return null;
  }

  private String getGoogleIdToken() throws IOException {
    GoogleCredentials googleCredentials = GoogleCredentials.getApplicationDefault();

    IdTokenCredentials idTokenCredentials =
        IdTokenCredentials.newBuilder()
            .setIdTokenProvider((IdTokenProvider) googleCredentials)
            .setTargetAudience(SERVICE_URL)
            .setOptions(Arrays.asList(Option.FORMAT_FULL, Option.LICENSES_TRUE))
            .build();

    return idTokenCredentials.refreshAccessToken().getTokenValue();
  }

  @Test
  public void testValidToken() throws Exception {
    String token = getGoogleIdToken();
    HttpRequest request = buildRequest("Authorization", "bearer " + token);
    HttpResponse<String> response = executeRequest(request);

    assertTrue(response != null);
    assertTrue(response.statusCode() == HttpStatusCodes.STATUS_CODE_OK);
    assertTrue(response.body().contains("Hello,"));
    assertTrue(response.body().contains("@"));
  }

  @Test
  public void testInvalidToken() throws Exception {
    String token = "invalid_token";
    HttpRequest request = buildRequest("Authorization", "bearer " + token);
    HttpResponse<String> response = executeRequest(request);

    assertTrue(response != null);
    assertTrue(response.statusCode() == HttpStatusCodes.STATUS_CODE_UNAUTHORIZED);
    assertTrue(response.body().contains("Please supply a valid bearer token."));
  }

  @Test
  public void testAnonymousRequest() throws Exception {
    HttpRequest request = buildRequest(null, null);
    HttpResponse<String> response = executeRequest(request);

    assertTrue(response != null);
    assertTrue(response.statusCode() == HttpStatusCodes.STATUS_CODE_UNAUTHORIZED);
    assertTrue(response.body().contains("missing Authorization header"));
  }
}
