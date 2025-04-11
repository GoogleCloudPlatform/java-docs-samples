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

/**
 * This class demonstrates how to list templates using the Model Armor API.
 *
 * @author [Your Name]
 */

// [START modelarmor_list_templates]

import com.google.cloud.modelarmor.v1.ListTemplatesRequest;
import com.google.cloud.modelarmor.v1.LocationName;
import com.google.cloud.modelarmor.v1.ModelArmorClient;
import com.google.cloud.modelarmor.v1.ModelArmorSettings;
import com.google.cloud.modelarmor.v1.Template;
import com.google.protobuf.util.JsonFormat;

/** This class contains a main method that lists templates using the Model Armor API. */
public class ListTemplates {

  /**
   * Main method that calls the listTemplates method to list templates.
   *
   * @param args command line arguments (not used)
   * @throws Exception if an error occurs during template listing
   */
  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "your-project-id";
    String locationId = "your-location-id";

    listTemplates(projectId, locationId);
  }

  /**
   * Lists templates using the Model Armor API.
   *
   * @param projectId the ID of the project
   * @param locationId the ID of the location
   * @throws Exception if an error occurs during template listing
   */
  public static void listTemplates(String projectId, String locationId) throws Exception {
    // Construct the API endpoint URL
    String apiEndpoint = String.format("modelarmor.%s.rep.googleapis.com:443", locationId);

    // Create a Model Armor settings object with the API endpoint
    ModelArmorSettings modelArmorSettings =
        ModelArmorSettings.newBuilder().setEndpoint(apiEndpoint).build();

    try (ModelArmorClient client = ModelArmorClient.create(modelArmorSettings)) {
      // Construct the parent resource name
      String parent = LocationName.of(projectId, locationId).toString();

      // Create a list templates request object
      ListTemplatesRequest request = ListTemplatesRequest.newBuilder().setParent(parent).build();

      // List templates using the Model Armor client
      for (Template template : client.listTemplates(request).iterateAll()) {
        // Print each retrieved template
        System.out.println("Retrieved Templates: " + JsonFormat.printer().print(template));
      }
    }
  }
}

// [END modelarmor_list_templates]
