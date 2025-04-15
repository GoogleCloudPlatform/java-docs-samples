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

// [START modelarmor_update_template_labels]

import com.google.cloud.modelarmor.v1.ModelArmorClient;
import com.google.cloud.modelarmor.v1.ModelArmorSettings;
import com.google.cloud.modelarmor.v1.Template;
import com.google.cloud.modelarmor.v1.TemplateName;
import com.google.cloud.modelarmor.v1.UpdateTemplateRequest;
import com.google.protobuf.FieldMask;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class UpdateTemplateWithLabels {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.

    // Specify the Google Project ID.
    String projectId = "your-project-id";
    // Specify the location ID. For example, us-central1. 
    String locationId = "your-location-id";
    // Specify the template ID.
    String templateId = "your-template-id";

    updateTemplateWithLabels(projectId, locationId, templateId);
  }

  public static Template updateTemplateWithLabels(String projectId, String locationId,
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

      // Create a new labels map.
      Map<String, String> labels = new HashMap<>();
      
      // Add or update labels.
      labels.put("key1", "value2");
      labels.put("key2", "value3");

      // Update the template with the new labels.
      Template template = Template.newBuilder()
          .setName(name)
          .putAllLabels(labels)
          .build();

      // Create a field mask to specify that only labels should be updated.
      FieldMask updateMask = FieldMask.newBuilder()
          .addPaths("labels")
          .build();

      UpdateTemplateRequest request =
          UpdateTemplateRequest.newBuilder()
              .setTemplate(template)
              .setUpdateMask(updateMask)
              .build();

      Template updatedTemplate = client.updateTemplate(request);
      System.out.println("Updated template labels: " + updatedTemplate.getName());

      return updatedTemplate;
    }
  }
}

// [END modelarmor_update_template_labels]
