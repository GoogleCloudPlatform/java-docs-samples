/*
 * Copyright 2022 Google LLC
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

package snippets.healthcare.fhir.resources;

// [START healthcare_enable_implementation_guide]
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.healthcare.v1.CloudHealthcare;
import com.google.api.services.healthcare.v1.CloudHealthcare.Projects.Locations.Datasets.FhirStores;
import com.google.api.services.healthcare.v1.CloudHealthcareScopes;
import com.google.api.services.healthcare.v1.model.FhirStore;
import com.google.api.services.healthcare.v1.model.ValidationConfig;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FhirEnableImplementationGuide {

  private static final JsonFactory JSON_FACTORY = new GsonFactory();
  private static final NetHttpTransport HTTP_TRANSPORT = new NetHttpTransport();

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project-id";
    String location = "us-central1";
    String datasetId = "my-dataset-id";
    String fhirStoreId = "my-fhir-store-id";
    String implementationGuideUrl =
        "http://hl7.org/fhir/us/core/ImplementationGuide/hl7.fhir.us.core";
    fhirEnableImplementationGuide(
        projectId, location, datasetId, fhirStoreId, implementationGuideUrl);
  }
  ;

  // Enables a FHIR implementation guide on an existing FHIR store.
  public static void fhirEnableImplementationGuide(
      String projectId,
      String location,
      String datasetId,
      String fhirStoreId,
      String implementationGuideUrl)
      throws IOException {

    String fhirStoreName =
        String.format(
            "projects/%s/locations/%s/datasets/%s/fhirStores/%s",
            projectId, location, datasetId, fhirStoreId);

    // Initialize the client, which will be used to interact with the service.
    CloudHealthcare client = createClient();

    // Fetch the initial state of the FHIR store.
    FhirStores.Get getRequest =
        client.projects().locations().datasets().fhirStores().get(fhirStoreName);
    FhirStore store = getRequest.execute();

    List<String> implementationGuideUrls = new ArrayList<String>();
    implementationGuideUrls.add(implementationGuideUrl);
    ValidationConfig validationConfig = new ValidationConfig();
    validationConfig.setEnabledImplementationGuides(implementationGuideUrls);
    store.setValidationConfig(validationConfig);

    // Create PATCH request and configure any parameters.
    FhirStores.Patch request =
        client
            .projects()
            .locations()
            .datasets()
            .fhirStores()
            .patch(fhirStoreName, store)
            .setUpdateMask("validationConfig");

    // Execute the request and process the results.
    store = request.execute();
    System.out.println("ImplementationGuide enabled: \n" + store.toPrettyString());
  }

  // Initialize client that will be used to send requests. This client only needs to be created
  // once, and can be reused for multiple requests.
  private static CloudHealthcare createClient() throws IOException {
    // Use Application Default Credentials (ADC) to authenticate the requests
    // For more information see https://cloud.google.com/docs/authentication/production
    GoogleCredentials credential =
        GoogleCredentials.getApplicationDefault()
            .createScoped(Collections.singleton(CloudHealthcareScopes.CLOUD_PLATFORM));

    // Create an HttpRequestInitializer, which will provide a baseline configuration to all
    // requests.
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
// [END healthcare_enable_implementation_guide]
