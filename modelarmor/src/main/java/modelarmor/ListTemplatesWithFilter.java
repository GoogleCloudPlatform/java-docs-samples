/*
 * Copyright 2025 Google LLC
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

package modelarmor;

// [START modelarmor_list_templates_with_filter]

import com.google.cloud.modelarmor.v1.ListTemplatesRequest;
import com.google.cloud.modelarmor.v1.LocationName;
import com.google.cloud.modelarmor.v1.ModelArmorClient;
import com.google.cloud.modelarmor.v1.ModelArmorClient.ListTemplatesPagedResponse;
import com.google.cloud.modelarmor.v1.ModelArmorSettings;
import java.io.IOException;

public class ListTemplatesWithFilter {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.

    String projectId = "your-project-id";
    String locationId = "your-location-id";
    // Filter to applied.
    // Example: "name=\"projects/your-project-id/locations/us-central1/your-template-id\""
    String filter = "your-filter-condition";

    listTemplatesWithFilter(projectId, locationId, filter);
  }

  public static ListTemplatesPagedResponse listTemplatesWithFilter(String projectId,
      String locationId, String filter) throws IOException {
    // Construct the API endpoint URL.
    String apiEndpoint = String.format("modelarmor.%s.rep.googleapis.com:443", locationId);

    ModelArmorSettings modelArmorSettings = ModelArmorSettings.newBuilder().setEndpoint(apiEndpoint)
        .build();

    // Initialize the client that will be used to send requests. This client
    // only needs to be created once, and can be reused for multiple requests.
    try (ModelArmorClient client = ModelArmorClient.create(modelArmorSettings)) {
      // Build the parent name.
      String parent = LocationName.of(projectId, locationId).toString();

      ListTemplatesRequest request = ListTemplatesRequest.newBuilder()
          .setParent(parent)
          .setFilter(filter)
          .build();

      // List all templates.
      ListTemplatesPagedResponse pagedResponse = client.listTemplates(request);
      pagedResponse.iterateAll().forEach(template -> {
        System.out.printf("Template %s\n", template.getName());
      });

      return pagedResponse;
    }
  }
}

// [END modelarmor_list_templates_with_filter]
