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

// [START healthcare_list_hl7v2_messages]
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.healthcare.v1.CloudHealthcare;
import com.google.api.services.healthcare.v1.CloudHealthcare.Projects.Locations.Datasets.Hl7V2Stores.Messages;
import com.google.api.services.healthcare.v1.CloudHealthcareScopes;
import com.google.api.services.healthcare.v1.model.ListMessagesResponse;
import com.google.api.services.healthcare.v1.model.Message;
import java.io.IOException;
import java.util.Collections;

public class HL7v2MessageList {
  private static final String HL7v2_NAME = "projects/%s/locations/%s/datasets/%s/hl7V2Stores/%s";
  private static final JsonFactory JSON_FACTORY = new JacksonFactory();
  private static final NetHttpTransport HTTP_TRANSPORT = new NetHttpTransport();

  public static void hl7v2MessageList(String hl7v2StoreName) throws IOException {
    // String hl7v2StoreName =
    //    String.format(
    //        HL7v2_NAME, "your-project-id", "your-region-id", "your-dataset-id", "your-hl7v2-id");

    // Initialize the client, which will be used to interact with the service.
    CloudHealthcare client = createClient();

    // Results are paginated, so multiple queries may be required.
    String pageToken = null;

    // Create request and configure any parameters.
    try {
      Messages.List request =
          client
              .projects()
              .locations()
              .datasets()
              .hl7V2Stores()
              .messages()
              .list(hl7v2StoreName)
              .setPageSize(100) // Specify pageSize up to 1000
              .setPageToken(pageToken);

      ListMessagesResponse response;
      // Execute response and collect results.
      do {
        response = request.execute();
        if (response.getHl7V2Messages() == null) {
          continue;
        }
        System.out.printf("Retrieved %s HL7v2 messages: \n", response.getHl7V2Messages().size());
        for (Message message : response.getHl7V2Messages()) {
            System.out.println("\t" + message);
        }
        request.setPageToken(response.getNextPageToken());
       } while (response.getNextPageToken() != null);
    } catch (IOException e) {
      System.out.println("Unable to list HL7v2 messages:" + e.toString());
    }
  }

  private static CloudHealthcare createClient() throws IOException {
    // Use Application Default Credentials (ADC) to authenticate the requests
    // For more information see https://cloud.google.com/docs/authentication/production
    GoogleCredential credential =
        GoogleCredential.getApplicationDefault(HTTP_TRANSPORT, JSON_FACTORY)
            .createScoped(Collections.singleton(CloudHealthcareScopes.CLOUD_PLATFORM));

    // Create a HttpRequestInitializer, which will provide a baseline configuration to all requests.
    HttpRequestInitializer requestInitializer =
        request -> {
          credential.initialize(request);
          request.setConnectTimeout(60000); // 1 minute connect timeout
          request.setReadTimeout(60000); // 1 minute read timeout
        };

    // Build the client for interacting with the service.
    return new CloudHealthcare.Builder(HTTP_TRANSPORT, JSON_FACTORY, requestInitializer)
        .setApplicationName("your-application-name")
        .build();
  }
}
// [END healthcare_list_hl7v2_messages]
