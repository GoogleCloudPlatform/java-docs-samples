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

// [START modelarmor_update_template_metadata]

import com.google.cloud.modelarmor.v1.ModelArmorClient;
import com.google.cloud.modelarmor.v1.ModelArmorSettings;
import com.google.cloud.modelarmor.v1.Template;
import com.google.cloud.modelarmor.v1.Template.TemplateMetadata;
import com.google.cloud.modelarmor.v1.TemplateName;
import com.google.cloud.modelarmor.v1.UpdateTemplateRequest;
import com.google.protobuf.FieldMask;
import java.io.IOException;

public class UpdateTemplateWithMetadata {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.

    // Specify the Google Project ID.
    String projectId = "your-project-id";
    // Specify the location ID. For example, us-central1. 
    String locationId = "your-location-id";
    // Specify the template ID.
    String templateId = "your-template-id";

    updateTemplateWithMetadata(projectId, locationId, templateId);
  }

  public static Template updateTemplateWithMetadata(String projectId, String locationId,
      String templateId) throws IOException {
    // Construct the API endpoint URL.
    String apiEndpoint = String.format("modelarmor.%s.rep.googleapis.com:443", locationId);

    ModelArmorSettings modelArmorSettings = ModelArmorSettings.newBuilder().setEndpoint(apiEndpoint)
        .build();

    // Initialize the client that will be used to send requests. This client
    // only needs to be created once, and can be reused for multiple requests.
    try (ModelArmorClient client = ModelArmorClient.create(modelArmorSettings)) {
      // Get the template name.
      String name = TemplateName.of(projectId, locationId, templateId).toString();

      // For more details about metadata, refer to the following documentation:
      // https://cloud.google.com/security-command-center/docs/reference/model-armor/rest/v1/projects.locations.templates#templatemetadata
      TemplateMetadata updatedMetadata = TemplateMetadata.newBuilder()
          .setIgnorePartialInvocationFailures(false)
          .setLogSanitizeOperations(false)
          .setCustomPromptSafetyErrorCode(400)
          .build();

      // Update the template with new metadata.
      Template template = Template.newBuilder()
          .setName(name)
          .setTemplateMetadata(updatedMetadata)
          .build();

      // Create a field mask to specify which metadata fields should be updated.
      FieldMask updateMask = FieldMask.newBuilder()
          .addPaths("template_metadata")
          .build();

      UpdateTemplateRequest request =
          UpdateTemplateRequest.newBuilder()
              .setTemplate(template)
              .setUpdateMask(updateMask)
              .build();

      Template updatedTemplate = client.updateTemplate(request);
      System.out.println("Updated template metadata: " + updatedTemplate.getName());

      return updatedTemplate;
    }
  }
}

// [END modelarmor_update_template_metadata]
