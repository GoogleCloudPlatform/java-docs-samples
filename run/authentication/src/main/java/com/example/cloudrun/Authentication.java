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

package com.example.cloudrun;

// [START run_service_to_service_auth]
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Authentication {

  // Instantiate OkHttpClient
  private static final OkHttpClient ok =
      new OkHttpClient.Builder()
          .readTimeout(500, TimeUnit.MILLISECONDS)
          .writeTimeout(500, TimeUnit.MILLISECONDS)
          .build();

  // makeGetRequest makes a GET request to the specified Cloud Run endpoint,
  // serviceUrl (must be a complete URL), by authenticating with the Id token
  // obtained from the Metadata API.
  public static Response makeGetRequest(String serviceUrl) throws IOException {
    Request.Builder serviceRequest = new Request.Builder().url(serviceUrl);

    // Set up metadata server request
    // https://cloud.google.com/compute/docs/instances/verifying-instance-identity#request_signature
    String tokenUrl =
        String.format(
            "http://metadata/computeMetadata/v1/instance/service-accounts/default/identity?audience=%s",
            serviceUrl);
    Request tokenRequest =
        new Request.Builder().url(tokenUrl).addHeader("Metadata-Flavor", "Google").get().build();
    // Fetch the token
    try (Response tokenResponse = ok.newCall(tokenRequest).execute()) {
      String token = tokenResponse.body().string();
      // Provide the token in the request to the receiving service
      serviceRequest.addHeader("Authorization", "Bearer " + token);
      System.out.println("Successful Token");
    } catch (IOException e) {
      System.out.println("Id token query failed: " + e);
    }

    return ok.newCall(serviceRequest.get().build()).execute();
  }
}
// [END run_service_to_service_auth]
