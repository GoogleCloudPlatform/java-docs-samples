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

// [START functions_bearer_token]

import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.time.Duration;

public class BearerTokenHttp implements HttpFunction {

  // TODO<developer> specify values for these environment variables
  private static String REGION = System.getenv("TARGET_REGION");
  private static String PROJECT_ID = System.getenv("GCP_PROJECT");
  private static String RECEIVING_FUNCTION_NAME = "myFunction";

  private static String receivingFunctionUrl = String.format(
      "https://%s-%s.cloudfunctions.net/%s", REGION, PROJECT_ID, RECEIVING_FUNCTION_NAME);
  private static String metadataTokenEndpoint =
      "http://metadata/computeMetadata/v1/instance/service-accounts/default/identity?audience=";

  private static HttpClient client =
      HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

  @Override
  public void service(HttpRequest request, HttpResponse response)
      throws IOException, InterruptedException {

    // Set up metadata server request
    // See https://cloud.google.com/compute/docs/instances/verifying-instance-identity#request_signature
    java.net.http.HttpRequest tokenRequest = java.net.http.HttpRequest.newBuilder()
        .uri(URI.create(metadataTokenEndpoint + receivingFunctionUrl))
        .GET()
        .header("Metadata-Flavor", "Google")
        .build();

    // Fetch the bearer token
    java.net.http.HttpResponse<String> tokenReponse =
        client.send(tokenRequest, java.net.http.HttpResponse.BodyHandlers.ofString());
    String token = tokenReponse.body();

    // Pass the token along to receiving function
    java.net.http.HttpRequest functionRequest = java.net.http.HttpRequest.newBuilder()
        .uri(URI.create(receivingFunctionUrl))
        .GET()
        .header("Authorization", "Bearer " + token)
        .build();
    java.net.http.HttpResponse<String> functionResponse =
        client.send(functionRequest, java.net.http.HttpResponse.BodyHandlers.ofString());

    // Write the results to the output:
    BufferedWriter writer = response.getWriter();
    writer.write(functionResponse.body());
  }
}
// [END functions_bearer_token]
