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

// [START healthcare_dicom_keeplist_deidentify_dataset]
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.healthcare.v1.CloudHealthcare;
import com.google.api.services.healthcare.v1.CloudHealthcare.Projects.Locations.Datasets;
import com.google.api.services.healthcare.v1.CloudHealthcareScopes;
import com.google.api.services.healthcare.v1.model.DeidentifyConfig;
import com.google.api.services.healthcare.v1.model.DeidentifyDatasetRequest;
import com.google.api.services.healthcare.v1.model.DicomConfig;
import com.google.api.services.healthcare.v1.model.Operation;
import com.google.api.services.healthcare.v1.model.TagFilterList;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

public class DatasetDeIdentify {
  private static final String DATASET_NAME = "projects/%s/locations/%s/datasets/%s";
  private static final JsonFactory JSON_FACTORY = new JacksonFactory();
  private static final NetHttpTransport HTTP_TRANSPORT = new NetHttpTransport();

  public static void datasetDeIdentify(String srcDatasetName, String destDatasetName)
      throws IOException {
    // String srcDatasetName =
    //     String.format(DATASET_NAME, "your-project-id", "your-region-id", "your-src-dataset-id");
    // String destDatasetName =
    //    String.format(DATASET_NAME, "your-project-id", "your-region-id", "your-dest-dataset-id");

    // Initialize the client, which will be used to interact with the service.
    CloudHealthcare client = createClient();

    // Configure what information needs to be De-Identified.
    // For more information on de-identifying using tags, please see the following:
    // https://cloud.google.com/healthcare/docs/how-tos/dicom-deidentify#de-identification_using_tags
    TagFilterList tags = new TagFilterList().setTags(Arrays.asList("PatientID"));
    DicomConfig dicomConfig = new DicomConfig().setKeepList(tags);
    DeidentifyConfig config = new DeidentifyConfig().setDicom(dicomConfig);

    // Create the de-identify request and configure any parameters.
    DeidentifyDatasetRequest deidentifyRequest =
        new DeidentifyDatasetRequest().setDestinationDataset(destDatasetName).setConfig(config);
    Datasets.Deidentify request =
        client.projects().locations().datasets().deidentify(srcDatasetName, deidentifyRequest);

    // Execute the request, wait for the operation to complete, and process the results.
    try {
      Operation operation = request.execute();
      while (operation.getDone() == null || !operation.getDone()) {
        // Update the status of the operation with another request.
        Thread.sleep(500); // Pause for 500ms between requests.
        operation =
            client
                .projects()
                .locations()
                .datasets()
                .operations()
                .get(operation.getName())
                .execute();
      }
      System.out.println(
          "De-identified Dataset created. Response content: " + operation.getResponse());
    } catch (Exception ex) {
      System.out.printf("Error during request execution: %s", ex.toString());
      ex.printStackTrace(System.out);
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
// [END healthcare_dicom_keeplist_deidentify_dataset]
