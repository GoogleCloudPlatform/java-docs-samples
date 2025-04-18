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
import java.io.IOException;
import java.util.List;

public class CreateTemplate {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.

    // Specify the Google Project ID.
    String projectId = "your-project-id";
    // Specify the location ID. For example, us-central1. 
    String locationId = "your-location-id";
    // Specify the template ID.
    String templateId = "your-template-id";

    createTemplate(projectId, locationId, templateId);
  }

  public static Template createTemplate(String projectId, String locationId, String templateId)
      throws IOException {
    // Construct the API endpoint URL.
    String apiEndpoint = String.format("modelarmor.%s.rep.googleapis.com:443", locationId);
    ModelArmorSettings modelArmorSettings = ModelArmorSettings.newBuilder().setEndpoint(apiEndpoint)
        .build();

    // Initialize the client that will be used to send requests. This client
    // only needs to be created once, and can be reused for multiple requests.
    try (ModelArmorClient client = ModelArmorClient.create(modelArmorSettings)) {
      String parent = LocationName.of(projectId, locationId).toString();

      // Build the Model Armor template with your preferred filters.
      // For more details on filters, please refer to the following doc:
      // https://cloud.google.com/security-command-center/docs/key-concepts-model-armor#ma-filters

      // Configure Responsible AI filter with multiple categories and their confidence
      // levels.
      RaiFilterSettings raiFilterSettings =
          RaiFilterSettings.newBuilder()
              .addAllRaiFilters(
                  List.of(
                      RaiFilter.newBuilder()
                          .setFilterType(RaiFilterType.DANGEROUS)
                          .setConfidenceLevel(DetectionConfidenceLevel.HIGH)
                          .build(),
                      RaiFilter.newBuilder()
                          .setFilterType(RaiFilterType.HATE_SPEECH)
                          .setConfidenceLevel(DetectionConfidenceLevel.HIGH)
                          .build(),
                      RaiFilter.newBuilder()
                          .setFilterType(RaiFilterType.SEXUALLY_EXPLICIT)
                          .setConfidenceLevel(DetectionConfidenceLevel.LOW_AND_ABOVE)
                          .build(),
                      RaiFilter.newBuilder()
                          .setFilterType(RaiFilterType.HARASSMENT)
                          .setConfidenceLevel(DetectionConfidenceLevel.MEDIUM_AND_ABOVE)
                          .build()))
              .build();

      FilterConfig modelArmorFilter = FilterConfig.newBuilder()
          .setRaiSettings(raiFilterSettings)
          .build();

      Template template = Template.newBuilder()
          .setFilterConfig(modelArmorFilter)
          .build();

      CreateTemplateRequest request = CreateTemplateRequest.newBuilder()
          .setParent(parent)
          .setTemplateId(templateId)
          .setTemplate(template)
          .build();

      Template createdTemplate = client.createTemplate(request);
      System.out.println("Created template: " + createdTemplate.getName());

      return createdTemplate;
    }
  }
}

// [END modelarmor_create_template]
