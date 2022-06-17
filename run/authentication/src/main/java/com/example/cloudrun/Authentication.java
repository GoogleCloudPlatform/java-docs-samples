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

// [START cloudrun_service_to_service_auth]
// [START functions_bearer_token]
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.IdTokenCredentials;
import com.google.auth.oauth2.IdTokenProvider;
import java.io.IOException;

public class Authentication {

  // makeGetRequest makes a GET request to the specified Cloud Run or
  // Cloud Functions endpoint `serviceUrl` (must be a complete URL), by
  // authenticating with an ID token retrieved from Application Default
  // Credentials using the specified `audience`.
  //
  // [END functions_bearer_token]
  // Example `audience` value (Cloud Run): https://my-cloud-run-service.run.app/
  // [END cloudrun_service_to_service_auth]
  // [START functions_bearer_token]
  // Example `audience` value (Cloud Functions): https://project-region-projectid.cloudfunctions.net/myFunction
  // [START cloudrun_service_to_service_auth]
  public static HttpResponse makeGetRequest(String serviceUrl, String audience) throws IOException {
    GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();
    if (!(credentials instanceof IdTokenProvider)) {
      throw new IllegalArgumentException("Credentials are not an instance of IdTokenProvider.");
    }
    IdTokenCredentials tokenCredential =
        IdTokenCredentials.newBuilder()
            .setIdTokenProvider((IdTokenProvider) credentials)
            .setTargetAudience(audience)
            .build();

    GenericUrl genericUrl = new GenericUrl(serviceUrl);
    HttpCredentialsAdapter adapter = new HttpCredentialsAdapter(tokenCredential);
    HttpTransport transport = new NetHttpTransport();
    HttpRequest request = transport.createRequestFactory(adapter).buildGetRequest(genericUrl);
    return request.execute();
  }
}
// [END functions_bearer_token]
// [END cloudrun_service_to_service_auth]
