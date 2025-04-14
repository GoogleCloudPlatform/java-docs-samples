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

// [START modelarmor_create_template_with_labels]

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateTemplateWithLabels {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "your-project-id";
    String locationId = "your-location-id";
    String templateId = "your-template-id";

    createTemplateWithLabels(projectId, locationId, templateId);
  }

  public static Template createTemplateWithLabels(
      String projectId, String locationId, String templateId) throws IOException {
    String apiEndpoint = String.format("modelarmor.%s.rep.googleapis.com:443", locationId);
    ModelArmorSettings modelArmorSettings = ModelArmorSettings.newBuilder().setEndpoint(apiEndpoint)
        .build();

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

      // Create Labels.
      Map<String, String> labels = new HashMap<>();
      labels.put("key1", "value1");
      labels.put("key2", "value2");

      Template template = Template.newBuilder()
          .setFilterConfig(modelArmorFilter)
          .putAllLabels(labels)
          .build();

      CreateTemplateRequest request = CreateTemplateRequest.newBuilder()
          .setParent(parent)
          .setTemplateId(templateId)
          .setTemplate(template)
          .build();

      Template createdTemplate = client.createTemplate(request);
      System.out.println("Created template with labels: " + createdTemplate.getName());

      return createdTemplate;
    }
  }
}
// [END modelarmor_create_template_with_labels]
