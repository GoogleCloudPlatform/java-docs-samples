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

// [START modelarmor_create_template]

import com.google.cloud.modelarmor.v1.CreateTemplateRequest;
import com.google.cloud.modelarmor.v1.DetectionConfidenceLevel;
import com.google.cloud.modelarmor.v1.FilterConfig;
import com.google.cloud.modelarmor.v1.LocationName;
import com.google.cloud.modelarmor.v1.ModelArmorClient;
import com.google.cloud.modelarmor.v1.ModelArmorSettings;
import com.google.cloud.modelarmor.v1.RaiFilterSettings;
import com.google.cloud.modelarmor.v1.RaiFilterSettings.RaiFilter;
import com.google.cloud.modelarmor.v1.RaiFilterType;
import com.google.cloud.modelarmor.v1.Template;
import com.google.protobuf.util.JsonFormat;
import java.util.List;

/** This class contains a main method that creates a template using the Model Armor API. */
public class CreateTemplate {

  /**
   * Main method that calls the createTemplate method to create a template.
   *
   * @param args command line arguments (not used)
   * @throws Exception if an error occurs during template creation
   */
  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "your-project-id";
    String locationId = "your-location-id";
    String templateId = "your-template-id";

    createTemplate(projectId, locationId, templateId);
  }

  /**
   * Creates a template using the Model Armor API.
   *
   * @param projectId the ID of the project
   * @param locationId the ID of the location
   * @param templateId the ID of the template
   * @return the created template
   * @throws Exception if an error occurs during template creation
   */
  public static Template createTemplate(String projectId, String locationId, String templateId)
      throws Exception {
    // Construct the API endpoint URL
    String apiEndpoint = String.format("modelarmor.%s.rep.googleapis.com:443", locationId);

    // Create a Model Armor settings object with the API endpoint
    ModelArmorSettings modelArmorSettings =
        ModelArmorSettings.newBuilder().setEndpoint(apiEndpoint).build();

    try (ModelArmorClient client = ModelArmorClient.create(modelArmorSettings)) {
      // Construct the parent resource name
      String parent = LocationName.of(projectId, locationId).toString();

      // Create a template object with a filter config
      Template template =
          Template.newBuilder()
              .setFilterConfig(
                  FilterConfig.newBuilder()
                      .setRaiSettings(
                          RaiFilterSettings.newBuilder()
                              .addAllRaiFilters(
                                  List.of(
                                      RaiFilter.newBuilder()
                                          .setFilterType(RaiFilterType.DANGEROUS)
                                          .setConfidenceLevel(DetectionConfidenceLevel.HIGH)
                                          .build()))
                              .build())
                      .build())
              .build();

      // Create a create template request object
      CreateTemplateRequest request =
          CreateTemplateRequest.newBuilder()
              .setParent(parent)
              .setTemplateId(templateId)
              .setTemplate(template)
              .build();

      // Create the template using the Model Armor client
      Template createdTemplate = client.createTemplate(request);

      // Print the created template
      System.out.println("Created template: " + JsonFormat.printer().print(createdTemplate));

      return createdTemplate;
    }
  }
}

// [END modelarmor_create_template]
