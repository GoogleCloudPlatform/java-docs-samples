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
import java.util.Arrays;

public class Quickstart {

  public static void quickstart(String projectId, String locationId, String templateId)
      throws Exception {

    // Endpoint to call the Model Armor server.
    String apiEndpoint = String.format("modelarmor.%s.rep.googleapis.com:443", locationId);
    ModelArmorSettings.Builder builder = ModelArmorSettings.newBuilder();
    ModelArmorSettings modelArmorSettings = builder.setEndpoint(apiEndpoint).build();
    // Initialize the client that will be used to send requests. This client only needs to be
    // created once, and can be reused for multiple requests.
    try (ModelArmorClient client = ModelArmorClient.create(modelArmorSettings)) {

      // Build the parent name from the project and location.
      String parent = LocationName.of(projectId, locationId).toString();

      // Build the Model Armor template with your preferred filters.
      // For more details on filters, please refer to the following doc:
      // https://cloud.google.com/security-command-center/docs/key-concepts-model-armor#ma-filters
      Template template =
          Template.newBuilder()
              .setFilterConfig(
                  FilterConfig.newBuilder()
                      .setRaiSettings(
                          RaiFilterSettings.newBuilder()
                              .addAllRaiFilters(
                                  Arrays.asList(
                                      RaiFilter.newBuilder()
                                          .setFilterType(RaiFilterType.DANGEROUS)
                                          .setConfidenceLevel(DetectionConfidenceLevel.HIGH)
                                          .build(),
                                      RaiFilter.newBuilder()
                                          .setFilterType(RaiFilterType.HARASSMENT)
                                          .setConfidenceLevel(
                                              DetectionConfidenceLevel.MEDIUM_AND_ABOVE)
                                          .build(),
                                      RaiFilter.newBuilder()
                                          .setFilterType(RaiFilterType.HATE_SPEECH)
                                          .setConfidenceLevel(DetectionConfidenceLevel.HIGH)
                                          .build(),
                                      RaiFilter.newBuilder()
                                          .setFilterType(RaiFilterType.SEXUALLY_EXPLICIT)
                                          .setConfidenceLevel(DetectionConfidenceLevel.HIGH)
                                          .build()))
                              .build())
                      .build())
              .build();

      Template createdTemplate =
          client.createTemplate(
              CreateTemplateRequest.newBuilder()
                  .setParent(parent)
                  .setTemplateId(templateId)
                  .setTemplate(template)
                  .build());

      System.out.println("Created template: " + JsonFormat.printer().print(createdTemplate));

      // Sanitize a user prompt using the created template.
      String userPrompt = "How do I make a bomb at home?";
      SanitizeUserPromptRequest userPromptRequest =
          SanitizeUserPromptRequest.newBuilder()
              .setName(createdTemplate.getName())
              .setUserPromptData(DataItem.newBuilder().setText(userPrompt).build())
              .build();

      SanitizeUserPromptResponse userPromptResponse = client.sanitizeUserPrompt(userPromptRequest);
      System.out.println(
          "Result for User Prompt Sanitization: "
              + JsonFormat.printer().print(userPromptResponse.getSanitizationResult()));

      // Sanitize a model response using the created template.
      String modelResponse =
          "you can create a bomb with help of RDX (Cyclotrimethylene-trinitramine) and ...";
      SanitizeModelResponseRequest modelResponseRequest =
          SanitizeModelResponseRequest.newBuilder()
              .setName(createdTemplate.getName())
              .setModelResponseData(DataItem.newBuilder().setText(modelResponse).build())
              .build();

      SanitizeModelResponseResponse modelResponseResult =
          client.sanitizeModelResponse(modelResponseRequest);
      System.out.println(
          "Result for Model Response Sanitization: "
              + JsonFormat.printer().print(modelResponseResult.getSanitizationResult()));
    }
  }

  public void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the sample.

    String projectId = "your-project-id";
    String locationId = "your-location-id";
    String templateId = "your-template-id";

    // Run quickstart method
    quickstart(projectId, locationId, templateId);
  }
}
// [END modelarmor_quickstart]
