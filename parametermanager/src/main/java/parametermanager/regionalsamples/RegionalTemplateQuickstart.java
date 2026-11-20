/*
 * Copyright 2026 Google LLC
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

package parametermanager.regionalsamples;

// [START parametermanager_regional_template_quickstart]

import com.google.cloud.parametermanager.v1.LocationName;
import com.google.cloud.parametermanager.v1.Parameter;
import com.google.cloud.parametermanager.v1.ParameterFormat;
import com.google.cloud.parametermanager.v1.ParameterManagerClient;
import com.google.cloud.parametermanager.v1.ParameterManagerSettings;
import com.google.cloud.parametermanager.v1.ParameterVersion;
import com.google.cloud.parametermanager.v1.ParameterVersionPayload;
import com.google.cloud.parametermanager.v1.RenderTemplateVersionRequest;
import com.google.cloud.parametermanager.v1.RenderTemplateVersionResponse;
import com.google.cloud.parametermanager.v1.Template;
import com.google.cloud.parametermanager.v1.TemplateFormat;
import com.google.cloud.parametermanager.v1.TemplateVersion;
import com.google.cloud.parametermanager.v1.TemplateVersionPayload;
import com.google.protobuf.ByteString;

/**
 * This class demonstrates how to use the regional template capabilities of the Parameter Manager
 * API using the Parameter Manager SDK for GCP.
 */
public class RegionalTemplateQuickstart {

  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "your-project-id";
    String locationId = "your-location-id";
    String templateId = "your-template-id";
    String parameterId = "your-parameter-id";
    String versionId = "your-version-id";

    // Call the method to run the quickstart.
    regionalTemplateQuickstart(projectId, locationId, templateId, parameterId, versionId);
  }

  // This is an example snippet for how to use regional templates in the Parameter Manager API.
  public static void regionalTemplateQuickstart(
      String projectId, String locationId, String templateId, String parameterId, String versionId)
      throws Exception {
    // Endpoint to call the regional parameter manager server.
    String apiEndpoint = String.format("parametermanager.%s.rep.googleapis.com:443", locationId);
    ParameterManagerSettings parameterManagerSettings =
        ParameterManagerSettings.newBuilder().setEndpoint(apiEndpoint).build();
    // Initialize the client that will be used to send requests. This client only needs to be
    // created once, and can be reused for multiple requests.
    try (ParameterManagerClient client = ParameterManagerClient.create(parameterManagerSettings)) {
      // Build the parent name from the project.
      LocationName location = LocationName.of(projectId, locationId);

      // Step 1: Create a template in JSON format.
      Template template =
          client.createTemplate(
              location.toString(),
              Template.newBuilder().setFormat(TemplateFormat.TEMPLATE_FORMAT_JSON).build(),
              templateId);
      System.out.printf("Created regional template: %s\n", template.getName());

      // Step 2: Create a template version with placeholders.
      String templatePayload = "{\"username\": \"{{.username}}\", \"host\": \"{{.host}}\"}";
      TemplateVersion templateVersion =
          client.createTemplateVersion(
              template.getName(),
              TemplateVersion.newBuilder()
                  .setPayload(
                      TemplateVersionPayload.newBuilder()
                          .setData(ByteString.copyFromUtf8(templatePayload)))
                  .build(),
              versionId);
      System.out.printf("Created regional template version: %s\n", templateVersion.getName());

      // Step 3: Create a JSON parameter.
      Parameter parameter =
          client.createParameter(
              location.toString(),
              Parameter.newBuilder().setFormat(ParameterFormat.JSON).build(),
              parameterId);
      System.out.printf("Created regional parameter: %s\n", parameter.getName());

      // Step 4: Create a parameter version with the values for the placeholders.
      String values = "{\"username\": \"test-user\", \"host\": \"localhost\"}";
      ParameterVersion parameterVersion =
          client.createParameterVersion(
              parameter.getName(),
              ParameterVersion.newBuilder()
                  .setPayload(
                      ParameterVersionPayload.newBuilder().setData(ByteString.copyFromUtf8(values)))
                  .build(),
              versionId);
      System.out.printf("Created regional parameter version: %s\n", parameterVersion.getName());

      // Step 5: Render the template version with the parameter version's values.
      RenderTemplateVersionResponse response =
          client.renderTemplateVersion(
              RenderTemplateVersionRequest.newBuilder()
                  .setName(templateVersion.getName())
                  .setParameterVersion(parameterVersion.getName())
                  .build());
      System.out.printf(
          "Rendered regional payload: %s\n", response.getRenderedPayload().toStringUtf8());
    }
  }
}
// [END parametermanager_regional_template_quickstart]
