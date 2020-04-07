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

package com.example.functions.http;

// [START functions_concepts_requests]

import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;

public class SendHttpRequest implements HttpFunction {

  // Create a client with some reasonable defaults. This client can be reused for multiple requests.
  private static HttpClient client =
      HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

  @Override
  public void service(HttpRequest request, HttpResponse response)
      throws IOException, InterruptedException {
    // Create a GET sendHttpRequest to "http://example.com"
    String url = "http://example.com";
    java.net.http.HttpRequest getRequest =
        java.net.http.HttpRequest.newBuilder().uri(URI.create(url)).GET().build();

    // Send the sendHttpRequest using the client
    java.net.http.HttpResponse<String> getResponse =
        client.send(getRequest, BodyHandlers.ofString());

    // Write the results to the output:
    BufferedWriter writer = response.getWriter();
    writer.write(String.format("Received code '%s' from url '%s'.", getResponse.statusCode(), url));
  }
}
// [END functions_concepts_requests]
