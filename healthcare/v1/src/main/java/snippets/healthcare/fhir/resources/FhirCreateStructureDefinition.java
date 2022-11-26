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

// [START healthcare_create_structure_definition]
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.healthcare.v1.CloudHealthcare;
import com.google.api.services.healthcare.v1.CloudHealthcareScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;

public class FhirCreateStructureDefinition {

  private static final JsonFactory JSON_FACTORY = new JacksonFactory();
  private static final NetHttpTransport HTTP_TRANSPORT = new NetHttpTransport();

  public static void main(String[] args) throws IOException, URISyntaxException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project-id";
    String location = "us-central1";
    String datasetId = "my-dataset-id";
    String fhirStoreId = "my-fhir-store-id";
    String filePath = "StructureDefinition.json";
    fhirCreateStructureDefinition(projectId, location, datasetId, fhirStoreId, filePath);
  }
  ;

  // Creates a FHIR StructureDefinition resource in an existing FHIR store.
  public static void fhirCreateStructureDefinition(
      String projectId, String location, String datasetId, String fhirStoreId, String filePath)
      throws IOException, URISyntaxException {

    String fhirStoreName =
        String.format(
            "projects/%s/locations/%s/datasets/%s/fhirStores/%s",
            projectId, location, datasetId, fhirStoreId);

    // Initialize the client, which will be used to interact with the service.
    CloudHealthcare client = createClient();

    HttpClient httpClient = HttpClients.createDefault();
    String uri =
        String.format("%sv1/%s/fhir/StructureDefinition", client.getRootUrl(), fhirStoreName);
    URIBuilder uriBuilder = new URIBuilder(uri).setParameter("access_token", getAccessToken());

    // Load the data from JSON file containing the StructureDefinition resource.
    List<String> lines = Files.readAllLines(Paths.get(filePath), Charset.defaultCharset());
    String data = String.join("\n", lines);
    StringEntity requestEntity = new StringEntity(data);

    HttpUriRequest request =
        RequestBuilder.post()
            .setUri(uriBuilder.build())
            .setEntity(requestEntity)
            .addHeader("Content-Type", "application/fhir+json")
            .addHeader("Accept-Charset", "utf-8")
            .build();

    // Execute the request and process the results.
    HttpResponse response = httpClient.execute(request);
    HttpEntity responseEntity = response.getEntity();
    if (response.getStatusLine().getStatusCode() != HttpStatus.SC_CREATED) {
      System.err.print(
          String.format(
              "Exception creating FHIR StructureDefinition resource: %s\n",
              response.getStatusLine().toString()));
      responseEntity.writeTo(System.err);
      throw new RuntimeException();
    }
    System.out.print("FHIR StructureDefinition resource created: ");
    responseEntity.writeTo(System.out);
  }

  // Initialize client that will be used to send requests. This client only needs to be created
  // once, and can be reused for multiple requests.
  private static CloudHealthcare createClient() throws IOException {
    // Use Application Default Credentials (ADC) to authenticate the requests
    // For more information see
    // https://cloud.google.com/docs/authentication/production
    GoogleCredentials credential =
        GoogleCredentials.getApplicationDefault()
            .createScoped(Collections.singleton(CloudHealthcareScopes.CLOUD_PLATFORM));

    // Create a HttpRequestInitializer, which will provide a baseline configuration
    // to all requests.
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

  private static String getAccessToken() throws IOException {
    GoogleCredentials credential =
        GoogleCredentials.getApplicationDefault()
            .createScoped(Collections.singleton(CloudHealthcareScopes.CLOUD_PLATFORM));

    return credential.refreshAccessToken().getTokenValue();
  }
}

// [END healthcare_create_structure_definition]
