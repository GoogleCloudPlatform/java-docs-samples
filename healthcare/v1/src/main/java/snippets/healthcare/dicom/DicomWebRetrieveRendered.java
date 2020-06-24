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

package snippets.healthcare.dicom;

// [START healthcare_dicomweb_retrieve_rendered]
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.healthcare.v1.CloudHealthcare;
import com.google.api.services.healthcare.v1.CloudHealthcare.Projects.Locations.Datasets.DicomStores.Studies.Series.Instances;
import com.google.api.services.healthcare.v1.CloudHealthcareScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;

public class DicomWebRetrieveRendered {
  private static final String DICOM_NAME = "projects/%s/locations/%s/datasets/%s/dicomStores/%s";
  private static final String DICOMWEB_PATH = "studies/%s/series/%s/instances/%s/rendered";
  private static final JsonFactory JSON_FACTORY = new JacksonFactory();
  private static final NetHttpTransport HTTP_TRANSPORT = new NetHttpTransport();

  public static void dicomWebRetrieveRendered(String dicomStoreName, String dicomWebPath)
      throws IOException {
    // String dicomStoreName =
    //    String.format(
    //        DICOM_NAME, "your-project-id", "your-region-id", "your-dataset-id", "your-dicom-id");
    // String dicomWebPath = String.format(DICOMWEB_PATH, "your-study-id", "your-series-id",
    // "your-instance-id");

    // Initialize the client, which will be used to interact with the service.
    CloudHealthcare client = createClient();

    // Create request and configure any parameters.
    Instances.RetrieveRendered request =
        client
            .projects()
            .locations()
            .datasets()
            .dicomStores()
            .studies()
            .series()
            .instances()
            .retrieveRendered(dicomStoreName, dicomWebPath);

    // Execute the request and process the results.
    HttpResponse response = request.executeUnparsed();

    String outputPath = "image.png";
    OutputStream outputStream = new FileOutputStream(new File(outputPath));
    try {
      response.download(outputStream);
      System.out.println("DICOM rendered PNG image written to file " + outputPath);
    } finally {
      outputStream.close();
    }

    if (!response.isSuccessStatusCode()) {
      System.err.print(
          String.format(
              "Exception retrieving DICOM rendered image: %s\n", response.getStatusMessage()));
      throw new RuntimeException();
    }
  }

  private static CloudHealthcare createClient() throws IOException {
    // Use Application Default Credentials (ADC) to authenticate the requests
    // For more information see https://cloud.google.com/docs/authentication/production
    GoogleCredentials credential =
        GoogleCredentials.getApplicationDefault()
            .createScoped(Collections.singleton(CloudHealthcareScopes.CLOUD_PLATFORM));

    HttpHeaders headers = new HttpHeaders();
    headers.set("X-GFE-SSL", "yes");
    // Retrieve using the PNG consumer imaging format.
    headers.setAccept("image/png");
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
// [END healthcare_dicomweb_retrieve_rendered]
