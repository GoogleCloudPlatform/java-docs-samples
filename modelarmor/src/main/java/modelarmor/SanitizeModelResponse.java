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

// [START modelarmor_sanitize_model_response]

import com.google.cloud.modelarmor.v1.DataItem;
import com.google.cloud.modelarmor.v1.ModelArmorClient;
import com.google.cloud.modelarmor.v1.ModelArmorSettings;
import com.google.cloud.modelarmor.v1.SanitizeModelResponseRequest;
import com.google.cloud.modelarmor.v1.SanitizeModelResponseResponse;
import com.google.cloud.modelarmor.v1.TemplateName;
import com.google.protobuf.util.JsonFormat;
import java.io.IOException;

public class SanitizeModelResponse {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.

    // Specify the Google Project ID.
    String projectId = "your-project-id";
    // Specify the location ID. For example, us-central1. 
    String locationId = "your-location-id";
    // Specify the template ID.
    String templateId = "your-template-id";
    // Specify the model response.
    String modelResponse = "Unsanitized model output";

    sanitizeModelResponse(projectId, locationId, templateId, modelResponse);
  }

  public static SanitizeModelResponseResponse sanitizeModelResponse(String projectId,
      String locationId, String templateId, String modelResponse) throws IOException {

    // Endpoint to call the Model Armor server.
    String apiEndpoint = String.format("modelarmor.%s.rep.googleapis.com:443", locationId);
    ModelArmorSettings modelArmorSettings = ModelArmorSettings.newBuilder().setEndpoint(apiEndpoint)
        .build();

    try (ModelArmorClient client = ModelArmorClient.create(modelArmorSettings)) {
      // Build the resource name of the template.
      String name = TemplateName.of(projectId, locationId, templateId).toString();

      // Prepare the request.
      SanitizeModelResponseRequest request = 
          SanitizeModelResponseRequest.newBuilder()
            .setName(name)
            .setModelResponseData(
              DataItem.newBuilder().setText(modelResponse)
              .build())
            .build();

      SanitizeModelResponseResponse response = client.sanitizeModelResponse(request);
      System.out.println("Result for the provided model response: "
          + JsonFormat.printer().print(response.getSanitizationResult()));

      return response;
    }
  }
}
// [END modelarmor_sanitize_model_response]
