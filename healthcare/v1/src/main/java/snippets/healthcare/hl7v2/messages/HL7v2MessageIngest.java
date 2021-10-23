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

package snippets.healthcare.hl7v2.messages;

// [START healthcare_ingest_hl7v2_message]
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.healthcare.v1.CloudHealthcare;
import com.google.api.services.healthcare.v1.CloudHealthcare.Projects.Locations.Datasets.Hl7V2Stores.Messages;
import com.google.api.services.healthcare.v1.CloudHealthcareScopes;
import com.google.api.services.healthcare.v1.model.IngestMessageRequest;
import com.google.api.services.healthcare.v1.model.IngestMessageResponse;
import com.google.api.services.healthcare.v1.model.Message;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public class HL7v2MessageIngest {
  private static final String HL7v2_NAME = "projects/%s/locations/%s/datasets/%s/hl7V2Stores/%s";
  private static final String MESSAGE_NAME =
      "projects/%s/locations/%s/datasets/%s/hl7V2Stores/%s/messages/%s";
  private static final JsonFactory JSON_FACTORY = new JacksonFactory();
  private static final NetHttpTransport HTTP_TRANSPORT = new NetHttpTransport();

  public static void hl7v2MessageIngest(String hl7v2StoreName, String filePath) throws IOException {
    // String hl7v2StoreName =
    //    String.format(
    //        HL7v2_NAME, "your-project-id", "your-region-id", "your-dataset-id", "your-hl7v2-id");

    // Initialize the client, which will be used to interact with the service.
    CloudHealthcare client = createClient();

    // Load the data from file and format it into an ingest request.
    List<String> lines = Files.readAllLines(Paths.get(filePath), Charset.defaultCharset());
    String data = String.join("\n", lines);
    Message message = new Message().setData(data);
    IngestMessageRequest ingestRequest = new IngestMessageRequest().setMessage(message);

    // Create request and configure any parameters.
    Messages.Ingest request =
        client
            .projects()
            .locations()
            .datasets()
            .hl7V2Stores()
            .messages()
            .ingest(hl7v2StoreName, ingestRequest);

    // Execute the request and process the results.
    IngestMessageResponse response = request.execute();
    System.out.println("HL7v2 message ingested: " + response.toPrettyString());
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
// [END healthcare_ingest_hl7v2_message]
