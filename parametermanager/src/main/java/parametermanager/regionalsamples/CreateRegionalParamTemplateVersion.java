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

// [START parametermanager_create_regional_param_template_version]

import com.google.cloud.parametermanager.v1.ParameterManagerClient;
import com.google.cloud.parametermanager.v1.ParameterManagerSettings;
import com.google.cloud.parametermanager.v1.TemplateName;
import com.google.cloud.parametermanager.v1.TemplateVersion;
import com.google.cloud.parametermanager.v1.TemplateVersionPayload;
import com.google.protobuf.ByteString;
import java.io.IOException;

/**
 * This class demonstrates how to create a regional template version using the Parameter Manager SDK
 * for GCP.
 */
public class CreateRegionalParamTemplateVersion {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "your-project-id";
    String locationId = "your-location-id";
    String templateId = "your-template-id";
    String versionId = "your-version-id";

    // Call the method to create a template version.
    createRegionalParamTemplateVersion(projectId, locationId, templateId, versionId);
  }

  // This is an example snippet for creating a regional template version with placeholders in its
  // payload.
  public static TemplateVersion createRegionalParamTemplateVersion(
      String projectId, String locationId, String templateId, String versionId) throws IOException {
    // Endpoint to call the regional parameter manager server.
    String apiEndpoint = String.format("parametermanager.%s.rep.googleapis.com:443", locationId);
    ParameterManagerSettings parameterManagerSettings =
        ParameterManagerSettings.newBuilder().setEndpoint(apiEndpoint).build();
    // Initialize the client that will be used to send requests. This client only needs to be
    // created once, and can be reused for multiple requests.
    try (ParameterManagerClient client = ParameterManagerClient.create(parameterManagerSettings)) {
      // Build the template name.
      TemplateName templateName = TemplateName.of(projectId, locationId, templateId);

      // Build the template version payload with placeholders.
      String payload = "{\"username\": \"{{.username}}\", \"host\": \"{{.host}}\"}";
      TemplateVersionPayload templateVersionPayload =
          TemplateVersionPayload.newBuilder().setData(ByteString.copyFromUtf8(payload)).build();

      // Build the template version to create.
      TemplateVersion templateVersion =
          TemplateVersion.newBuilder().setPayload(templateVersionPayload).build();

      // Create the template version.
      TemplateVersion createdTemplateVersion =
          client.createTemplateVersion(templateName.toString(), templateVersion, versionId);
      System.out.printf(
          "Created regional template version: %s\n", createdTemplateVersion.getName());

      return createdTemplateVersion;
    }
  }
}
// [END parametermanager_create_regional_param_template_version]
