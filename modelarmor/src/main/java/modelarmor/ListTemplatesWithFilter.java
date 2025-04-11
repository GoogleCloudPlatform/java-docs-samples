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
 * This class demonstrates how to list templates with a filter using the Model Armor API.
 *
 * @author [Your Name]
 */
import com.google.cloud.modelarmor.v1.ListTemplatesRequest;
import com.google.cloud.modelarmor.v1.LocationName;
import com.google.cloud.modelarmor.v1.ModelArmorClient;
import com.google.cloud.modelarmor.v1.ModelArmorSettings;
import com.google.cloud.modelarmor.v1.Template;
import com.google.protobuf.util.JsonFormat;

// [START modelarmor_list_templates_with_filter]

/**
 * This class contains a main method that lists templates with a filter using the Model Armor API.
 */
public class ListTemplatesWithFilter {

  /**
   * Main method that calls the listTemplatesWithFilter method to list templates with a filter.
   *
   * @param args command line arguments (not used)
   * @throws Exception if an error occurs during template listing
   */
  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "your-project-id";
    String locationId = "your-location-id";
    String templateId = "your-template-id";

    listTemplatesWithFilter(projectId, locationId, templateId);
  }

  /**
   * Lists templates with a filter using the Model Armor API.
   *
   * @param projectId the ID of the project
   * @param locationId the ID of the location
   * @param templateId the ID of the template
   * @throws Exception if an error occurs during template listing
   */
  public static void listTemplatesWithFilter(String projectId, String locationId, String templateId)
      throws Exception {
    // Construct the API endpoint URL
    String apiEndpoint = String.format("modelarmor.%s.rep.googleapis.com:443", locationId);

    // Create a Model Armor settings object with the API endpoint
    ModelArmorSettings modelArmorSettings =
        ModelArmorSettings.newBuilder().setEndpoint(apiEndpoint).build();

    try (ModelArmorClient client = ModelArmorClient.create(modelArmorSettings)) {
      // Construct the parent resource name
      String parent = LocationName.of(projectId, locationId).toString();

      // Construct the filter string
      String filter = String.format("name=\"%s/templates/%s\"", parent, templateId);

      // Create a list templates request object with the filter
      ListTemplatesRequest request =
          ListTemplatesRequest.newBuilder().setParent(parent).setFilter(filter).build();

      // List templates using the Model Armor client
      for (Template template : client.listTemplates(request).iterateAll()) {
        // Print each retrieved template
        System.out.println("Template with filter: " + JsonFormat.printer().print(template));
      }
    }
  }
}
