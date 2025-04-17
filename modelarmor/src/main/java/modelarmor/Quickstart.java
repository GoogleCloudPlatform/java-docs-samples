// Copyright 2025 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package modelarmor;

// [START modelarmor_quickstart]

import com.google.cloud.modelarmor.v1.CreateTemplateRequest;
import com.google.cloud.modelarmor.v1.DataItem;
import com.google.cloud.modelarmor.v1.DetectionConfidenceLevel;
import com.google.cloud.modelarmor.v1.FilterConfig;
import com.google.cloud.modelarmor.v1.LocationName;
import com.google.cloud.modelarmor.v1.ModelArmorClient;
import com.google.cloud.modelarmor.v1.ModelArmorSettings;
import com.google.cloud.modelarmor.v1.RaiFilterSettings;
import com.google.cloud.modelarmor.v1.RaiFilterSettings.RaiFilter;
import com.google.cloud.modelarmor.v1.RaiFilterType;
import com.google.cloud.modelarmor.v1.SanitizeModelResponseRequest;
import com.google.cloud.modelarmor.v1.SanitizeModelResponseResponse;
import com.google.cloud.modelarmor.v1.SanitizeUserPromptRequest;
import com.google.cloud.modelarmor.v1.SanitizeUserPromptResponse;
import com.google.cloud.modelarmor.v1.Template;
import com.google.protobuf.util.JsonFormat;
import java.util.List;

public class Quickstart {

  public void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the sample.

    // Specify the Google Project ID.
    String projectId = "your-project-id";
    // Specify the location ID. For example, us-central1. 
    String locationId = "your-location-id";
    // Specify the template ID.
    String templateId = "your-template-id";

    // Run quickstart method.
    quickstart(projectId, locationId, templateId);
  }

  // This is an example to demonstrate how to use Model Armor to screen
  // user prompts and model responses using a Model Armor template.
  public static void quickstart(String projectId, String locationId, String templateId)
      throws Exception {

    // Endpoint to call the Model Armor server.
    String apiEndpoint = String.format("modelarmor.%s.rep.googleapis.com:443", locationId);
    ModelArmorSettings.Builder builder = ModelArmorSettings.newBuilder();
    ModelArmorSettings modelArmorSettings = builder.setEndpoint(apiEndpoint).build();

    // Initialize the client that will be used to send requests. This client
    // only needs to be created once, and can be reused for multiple requests.
    try (ModelArmorClient client = ModelArmorClient.create(modelArmorSettings)) {

      // Build the parent name from the project and location.
      String parent = LocationName.of(projectId, locationId).toString();
      // Build the Model Armor template with your preferred filters.
      // For more details on filters, please refer to the following doc:
      // https://cloud.google.com/security-command-center/docs/key-concepts-model-armor#ma-filters

      // Configure Responsible AI filter with multiple categories and their
      // confidence levels.
      RaiFilterSettings raiFilterSettings = RaiFilterSettings.newBuilder()
          .addAllRaiFilters(
              List.of(
                  RaiFilter.newBuilder()
                      .setFilterType(RaiFilterType.DANGEROUS)
                      .setConfidenceLevel(DetectionConfidenceLevel.HIGH)
                      .build(),
                  RaiFilter.newBuilder()
                      .setFilterType(RaiFilterType.HATE_SPEECH)
                      .setConfidenceLevel(DetectionConfidenceLevel.MEDIUM_AND_ABOVE)
                      .build(),
                  RaiFilter.newBuilder()
                      .setFilterType(RaiFilterType.SEXUALLY_EXPLICIT)
                      .setConfidenceLevel(DetectionConfidenceLevel.MEDIUM_AND_ABOVE)
                      .build(),
                  RaiFilter.newBuilder()
                      .setFilterType(RaiFilterType.HARASSMENT)
                      .setConfidenceLevel(DetectionConfidenceLevel.MEDIUM_AND_ABOVE)
                      .build()))
          .build();

      FilterConfig modelArmorFilter =
          FilterConfig.newBuilder().setRaiSettings(raiFilterSettings).build();

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

      // Screen a user prompt using the created template.
      String userPrompt = "Unsafe user prompt";
      SanitizeUserPromptRequest userPromptRequest =
          SanitizeUserPromptRequest.newBuilder()
              .setName(createdTemplate.getName())
              .setUserPromptData(DataItem.newBuilder().setText(userPrompt).build())
              .build();

      SanitizeUserPromptResponse userPromptResponse = client.sanitizeUserPrompt(userPromptRequest);
      System.out.println(
          "Result for the provided user prompt: "
              + JsonFormat.printer().print(userPromptResponse.getSanitizationResult()));

      // Screen a model response using the created template.
      String modelResponse = "Unsanitized model output";
      SanitizeModelResponseRequest modelResponseRequest =
          SanitizeModelResponseRequest.newBuilder()
              .setName(createdTemplate.getName())
              .setModelResponseData(DataItem.newBuilder().setText(modelResponse).build())
              .build();

      SanitizeModelResponseResponse modelResponseResult =
          client.sanitizeModelResponse(modelResponseRequest);
      System.out.println(
          "Result for the provided model response: "
              + JsonFormat.printer().print(modelResponseResult.getSanitizationResult()));
    }
  }
}
// [END modelarmor_quickstart]
