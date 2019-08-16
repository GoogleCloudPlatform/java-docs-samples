/*
 * Copyright 2019 Google LLC
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

// [START functions_tips_scopes]
// [START run_tips_global_scope]
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class Tips {

  // Create a client with some reasonable defaults. This client can be reused for multiple requests.
  private static HttpClient client =
      HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

  // [END functions_tips_scopes]
  // [END run_tips_global_scope]
  private static int lightComputation() {
      List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9);
      return numbers.stream().reduce((t, x) -> t + x).get();
  }

  private static int heavyComputation() {
      List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9);
      return numbers.stream().reduce((t, x) -> t + x).get();
  }

  // [START functions_tips_scopes]
  // [START run_tips_global_scope]
  // Global (instance-wide) scope
  // This computation runs at instance cold-start
  public static final int INSTANCE_VAR = heavyComputation();

  /**
   * HTTP function that declares a variable.
   *
   * @param {Object} req request context.
   * @param {Object} res response context.
   */
  public void ScopesDemo(HttpServletRequest request, HttpServletResponse response)
      throws IOException, InterruptedException {
    // Per-function scope
    // This computation runs every time this function is called
    final int functionVar = lightComputation();
    PrintWriter writer = response.getWriter();
    writer.write(String.format("Per instance: %d, per function: %d", INSTANCE_VAR, functionVar));
  }
  // [END run_tips_global_scope]
  // [END functions_tips_scopes]

  public void TipsRetry(HttpServletRequest request, HttpServletResponse response)
      throws IOException, InterruptedException {
    // Create a GET TipsRetry to "http://example.com"
    String url = "http://example.com";
    HttpRequest getRequest = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();

    // Send the TipsRetry using the client
    HttpResponse<String> getResponse = client.send(getRequest, BodyHandlers.ofString());

    // Write the results to the output:
    PrintWriter writer = response.getWriter();
    writer.write(String.format("Received code '%s' from url '%s'.", getResponse.statusCode(), url));
  }
}

// [END functions_tips_scopes]
// [END run_tips_global_scope]
  