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

package snippets.healthcare.datasets;

// [START healthcare_list_datasets]
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.healthcare.v1.CloudHealthcare;
import com.google.api.services.healthcare.v1.CloudHealthcare.Projects.Locations.Datasets;
import com.google.api.services.healthcare.v1.CloudHealthcareScopes;
import com.google.api.services.healthcare.v1.model.Dataset;
import com.google.api.services.healthcare.v1.model.ListDatasetsResponse;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DatasetList {
  private static final JsonFactory JSON_FACTORY = new JacksonFactory();
  private static final NetHttpTransport HTTP_TRANSPORT = new NetHttpTransport();

  public static void datasetList(String projectId, String regionId) throws IOException {
    // String projectId = "your-project-id";
    // String regionId = "us-central1";

    // Initialize the client, which will be used to interact with the service.
    CloudHealthcare client = createClient();

    // Results are paginated, so multiple queries may be required.
    String parentName = String.format("projects/%s/locations/%s", projectId, regionId);
    String pageToken = null;
    List<Dataset> datasets = new ArrayList<>();
    do {
      // Create request and configure any parameters.
      Datasets.List request =
          client
              .projects()
              .locations()
              .datasets()
              .list(parentName)
              .setPageSize(100) // Specify pageSize up to 1000
              .setPageToken(pageToken);

      // Execute response and collect results.
      ListDatasetsResponse response = request.execute();
      datasets.addAll(response.getDatasets());

      // Update the page token for the next request.
      pageToken = response.getNextPageToken();
    } while (pageToken != null);

    // Print results.
    System.out.printf("Retrieved %s datasets: \n", datasets.size());
    for (Dataset data : datasets) {
      System.out.println("\t" + data.toPrettyString());
    }
  }

  private static CloudHealthcare createClient() throws IOException {
    // Use Application Default Credentials (ADC) to authenticate the requests
    // For more information see https://cloud.google.com/docs/authentication/production
    GoogleCredentials credential =
        GoogleCredentials.getApplicationDefault()
            .createScoped(Collections.singleton(CloudHealthcareScopes.CLOUD_PLATFORM));

    // Create a HttpRequestInitializer, which will provide a baseline configuration to all requests.
    HttpRequestInitializer requestInitializer =
        request -> {
          new HttpCredentialsAdapter(credential).initialize(request);
          request.setConnectTimeout(60000); // 1 minute connect timeout
          request.setReadTimeout(60000); // 1 minute read timeout
        };

    // Build the client for interacting with the service.
    return new CloudHealthcare.Builder(HTTP_TRANSPORT, JSON_FACTORY, requestInitializer)
        .setApplicationName("your-application-name")
        .build();
  }
}
// [END healthcare_list_datasets]
