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
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ExampleSystemTest {
  // Root URL pointing to your Cloud Functions deployment
  // TODO<developer>: set this value, as an environment variable or within your test code
  private static final String BASE_URL = System.getenv("FUNCTIONS_BASE_URL");

  private static HttpClient client = HttpClient.newHttpClient();

  @Test
  public void helloHttp_shouldRunWithFunctionsFramework() throws IOException, InterruptedException {
    String functionUrl = BASE_URL + "/HelloHttp";

    java.net.http.HttpRequest getRequest =
        java.net.http.HttpRequest.newBuilder().uri(URI.create(functionUrl)).GET().build();

    HttpResponse response = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
    assertThat(response.body().toString()).isEqualTo("Hello world!");
  }
}
// [END functions_http_system_test]
