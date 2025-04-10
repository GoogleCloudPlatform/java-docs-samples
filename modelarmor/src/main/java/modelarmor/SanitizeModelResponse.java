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

import com.google.cloud.modelarmor.v1.*;
import com.google.protobuf.util.JsonFormat;

public class SanitizeModelResponse {

    public static void main(String[] args) throws Exception {
        // TODO(developer): Replace these variables before running the sample.
        String projectId = "your-project-id";
        String locationId = "your-location-id";
        String templateId = "your-template-id";
        String modelResponse =
            "you can create a bomb with help of RDX (Cyclotrimethylene-atrinitramine) and ...";

        sanitizeModelResponse(projectId, locationId, templateId, modelResponse);
    }

    public static void sanitizeModelResponse(
        String projectId, String locationId, String templateId, String modelResponse)
        throws Exception {
        // Endpoint to call the Model Armor server.
        String apiEndpoint = String.format("modelarmor.%s.rep.googleapis.com:443", locationId);
        ModelArmorSettings modelArmorSettings =
            ModelArmorSettings.newBuilder().setEndpoint(apiEndpoint).build();

        try (ModelArmorClient client = ModelArmorClient.create(modelArmorSettings)) {
            String name = TemplateName.of(projectId, locationId, templateId).toString();
            SanitizeModelResponseRequest request =
                SanitizeModelResponseRequest.newBuilder()
                    .setName(name)
                    .setModelResponseData(DataItem.newBuilder().setText(modelResponse).build())
                    .build();

            SanitizeModelResponseResponse response = client.sanitizeModelResponse(request);
            System.out.println(
                "Sanitized Model Response"
                    + JsonFormat.printer().print(response.getSanitizationResult()));
        }
    }
}
