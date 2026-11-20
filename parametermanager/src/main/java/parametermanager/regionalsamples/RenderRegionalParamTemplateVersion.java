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

// [START parametermanager_render_regional_param_template_version]

import com.google.cloud.parametermanager.v1.ParameterManagerClient;
import com.google.cloud.parametermanager.v1.ParameterManagerSettings;
import com.google.cloud.parametermanager.v1.ParameterVersionName;
import com.google.cloud.parametermanager.v1.RenderTemplateVersionRequest;
import com.google.cloud.parametermanager.v1.RenderTemplateVersionResponse;
import com.google.cloud.parametermanager.v1.TemplateVersionName;
import java.io.IOException;

/**
 * This class demonstrates how to render a regional template version using the Parameter Manager SDK
 * for GCP.
 */
public class RenderRegionalParamTemplateVersion {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "your-project-id";
    String locationId = "your-location-id";
    String templateId = "your-template-id";
    String versionId = "your-version-id";
    String parameterId = "your-parameter-id";
    String parameterVersionId = "your-parameter-version-id";

    // Call the method to render a template version.
    renderRegionalParamTemplateVersion(
        projectId, locationId, templateId, versionId, parameterId, parameterVersionId);
  }

  // This is an example snippet for rendering a regional template version with the values of a
  // parameter version.
  public static RenderTemplateVersionResponse renderRegionalParamTemplateVersion(
      String projectId,
      String locationId,
      String templateId,
      String versionId,
      String parameterId,
      String parameterVersionId)
      throws IOException {
    // Endpoint to call the regional parameter manager server.
    String apiEndpoint = String.format("parametermanager.%s.rep.googleapis.com:443", locationId);
    ParameterManagerSettings parameterManagerSettings =
        ParameterManagerSettings.newBuilder().setEndpoint(apiEndpoint).build();
    // Initialize the client that will be used to send requests. This client only needs to be
    // created once, and can be reused for multiple requests.
    try (ParameterManagerClient client = ParameterManagerClient.create(parameterManagerSettings)) {
      // Build the template version name.
      TemplateVersionName templateVersionName =
          TemplateVersionName.of(projectId, locationId, templateId, versionId);

      // Build the parameter version name that supplies the values.
      ParameterVersionName parameterVersionName =
          ParameterVersionName.of(projectId, locationId, parameterId, parameterVersionId);

      // Build the render request.
      RenderTemplateVersionRequest request =
          RenderTemplateVersionRequest.newBuilder()
              .setName(templateVersionName.toString())
              .setParameterVersion(parameterVersionName.toString())
              .build();

      // Render the template version.
      RenderTemplateVersionResponse response = client.renderTemplateVersion(request);
      System.out.printf("Template payload: %s\n", response.getPayload().getData().toStringUtf8());
      System.out.printf(
          "Rendered regional payload: %s\n", response.getRenderedPayload().toStringUtf8());

      return response;
    }
  }
}
// [END parametermanager_render_regional_param_template_version]
