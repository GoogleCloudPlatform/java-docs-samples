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

package snippets.healthcare;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.healthcare.v1beta1.CloudHealthcare;
import com.google.api.services.healthcare.v1beta1.CloudHealthcareScopes;
import com.google.api.services.healthcare.v1beta1.model.ListDatasetsResponse;

import java.io.IOException;
import java.util.Collections;

public class TestBase {
  private static final JsonFactory JSON_FACTORY = new JacksonFactory();
  private static final NetHttpTransport HTTP_TRANSPORT = new NetHttpTransport();
  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String REGION_ID = "us-central1";

  protected static CloudHealthcare createClient() throws IOException {
    // Use Application Default Credentials (ADC) to authenticate the requests
    // For more information see https://cloud.google.com/docs/authentication/production
    GoogleCredential credential =
        GoogleCredential.getApplicationDefault(HTTP_TRANSPORT, JSON_FACTORY)
            .createScoped(Collections.singleton(CloudHealthcareScopes.CLOUD_PLATFORM));

    // Create a HttpRequestInitializer, which will provide a baseline configuration to all requests.
    HttpRequestInitializer requestInitializer =
        request -> {
          credential.initialize(request);
          request.setHeaders(new HttpHeaders().set("X-GFE-SSL", "yes"));
          request.setConnectTimeout(60000); // 1 minute connect timeout
          request.setReadTimeout(60000); // 1 minute read timeout
        };

    // Build the client for interacting with the service.
    return new CloudHealthcare.Builder(HTTP_TRANSPORT, JSON_FACTORY, requestInitializer)
        .setApplicationName("your-application-name")
        .build();
  }

  protected static void deleteDatasets() throws IOException {
    CloudHealthcare client = createClient();

    String parentName = String.format(
        "projects/%s/locations/%s",
        PROJECT_ID,
        REGION_ID);
    ListDatasetsResponse response = client
        .projects()
        .locations()
        .datasets()
        .list(parentName)
        .execute();
    if (response.getDatasets() == null) {
      return;
    }
    for (int i = 0; i < response.getDatasets().size(); i++) {
      client
          .projects()
          .locations()
          .datasets()
          .delete(response.getDatasets().get(i).getName())
          .execute();
    }
  }

}
