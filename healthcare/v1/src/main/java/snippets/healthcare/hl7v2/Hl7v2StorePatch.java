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

package snippets.healthcare.hl7v2;

// [START healthcare_patch_hl7v2_store]
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.healthcare.v1.CloudHealthcare;
import com.google.api.services.healthcare.v1.CloudHealthcare.Projects.Locations.Datasets.Hl7V2Stores;
import com.google.api.services.healthcare.v1.CloudHealthcareScopes;
import com.google.api.services.healthcare.v1.model.Hl7V2NotificationConfig;
import com.google.api.services.healthcare.v1.model.Hl7V2Store;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Hl7v2StorePatch {
  private static final String HL7v2_NAME = "projects/%s/locations/%s/datasets/%s/hl7V2Stores/%s";
  private static final JsonFactory JSON_FACTORY = new JacksonFactory();
  private static final NetHttpTransport HTTP_TRANSPORT = new NetHttpTransport();

  public static void patchHl7v2Store(String hl7v2StoreName, String pubsubTopic) throws IOException {
    // String hl7v2StoreName =
    //    String.format(
    //        HL7v2_NAME, "your-project-id", "your-region-id", "your-dataset-id", "your-hl7v2-id");
    // String pubsubTopic = "your-pubsub-topic";

    // Initialize the client, which will be used to interact with the service.
    CloudHealthcare client = createClient();

    // Fetch the initial state of the HL7v2 store.
    Hl7V2Stores.Get getRequest =
        client.projects().locations().datasets().hl7V2Stores().get(hl7v2StoreName);
    Hl7V2Store store = getRequest.execute();

    Hl7V2NotificationConfig notificationConfig = new Hl7V2NotificationConfig();
    notificationConfig.setPubsubTopic(pubsubTopic);
    List<Hl7V2NotificationConfig> notificationConfigs = new ArrayList<Hl7V2NotificationConfig>();
    notificationConfigs.add(notificationConfig);
    // Update the Hl7v2Store fields as needed as needed. For a full list of Hl7v2Store fields, see:
    // https://cloud.google.com/healthcare/docs/reference/rest/v1/projects.locations.datasets.hl7V2Store#Hl7v2Store
    store.setNotificationConfigs(notificationConfigs);

    // Create request and configure any parameters.
    Hl7V2Stores.Patch request =
        client
            .projects()
            .locations()
            .datasets()
            .hl7V2Stores()
            .patch(hl7v2StoreName, store)
            .setUpdateMask("notificationConfigs");

    // Execute the request and process the results.
    store = request.execute();
    System.out.println("HL7v2 store patched: \n" + store.toPrettyString());
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
// [END healthcare_patch_hl7v2_store]
