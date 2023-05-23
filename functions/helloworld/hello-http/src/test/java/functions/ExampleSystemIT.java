/*
 * Copyright 2020 Google LLC
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

package functions;

// [START functions_http_system_test]

import static com.google.common.truth.Truth.assertThat;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ExampleSystemIT {
  // Root URL pointing to your Cloud Functions deployment
  // TODO<developer>: set this value, as an environment variable or within your test code
  private static final String BASE_URL = System.getenv("FUNCTIONS_BASE_URL");

  // Identity token used to send requests to authenticated-only functions
  // TODO<developer>: Set this value if your function requires authentication.
  //                  See the documentation for more info:
  // https://cloud.google.com/functions/docs/securing/authenticating
  private static final String IDENTITY_TOKEN = System.getenv("FUNCTIONS_IDENTITY_TOKEN");

  // Name of the deployed function
  // TODO<developer>: Set this to HelloHttp, as an environment variable or within your test code
  private static final String FUNCTION_DEPLOYED_NAME = System.getenv("FUNCTIONS_HTTP_FN_NAME");

  private static HttpClient client = HttpClient.newHttpClient();

  @Test
  public void helloHttp_shouldRunWithFunctionsFramework() throws IOException, InterruptedException {
    String functionUrl = BASE_URL + "/" + FUNCTION_DEPLOYED_NAME;

    // [END functions_http_system_test]
    // Skip this test if FUNCTIONS_BASE_URL is not set
    if (BASE_URL == null) {
      System.out.println("FUNCTIONS_BASE_URL is not set; skipping ExampleSystemIT.");
      return;
    }

    // [START functions_http_system_test]
    HttpRequest.Builder getRequestBuilder = java.net.http.HttpRequest.newBuilder()
        .uri(URI.create(functionUrl))
        .GET();

    // Used to test functions that require authenticated invokers
    if (IDENTITY_TOKEN != null) {
      getRequestBuilder.header("Authorization", "Bearer " + IDENTITY_TOKEN);
    }

    java.net.http.HttpRequest getRequest = getRequestBuilder.build();

    HttpResponse<String> response = client.send(getRequest, HttpResponse.BodyHandlers.ofString());

    assertThat(response.statusCode()).isEqualTo(HttpURLConnection.HTTP_OK);
    assertThat(response.body().toString()).isEqualTo("Hello world!");
  }
}
// [END functions_http_system_test]
