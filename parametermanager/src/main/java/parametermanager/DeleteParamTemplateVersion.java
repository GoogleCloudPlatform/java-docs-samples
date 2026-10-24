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

package parametermanager;

// [START parametermanager_delete_param_template_version]

import com.google.cloud.parametermanager.v1.ParameterManagerClient;
import com.google.cloud.parametermanager.v1.TemplateVersionName;
import java.io.IOException;

/**
 * This class demonstrates how to delete a template version using the Parameter Manager SDK for GCP.
 */
public class DeleteParamTemplateVersion {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "your-project-id";
    String templateId = "your-template-id";
    String versionId = "your-version-id";

    // Call the method to delete a template version.
    deleteParamTemplateVersion(projectId, templateId, versionId);
  }

  // This is an example snippet for deleting a template version.
  public static void deleteParamTemplateVersion(
      String projectId, String templateId, String versionId) throws IOException {

    // Initialize the client that will be used to send requests. This client only needs to be
    // created once, and can be reused for multiple requests.
    try (ParameterManagerClient client = ParameterManagerClient.create()) {
      String locationId = "global";

      // Build the template version name.
      TemplateVersionName templateVersionName =
          TemplateVersionName.of(projectId, locationId, templateId, versionId);

      // Delete the template version.
      client.deleteTemplateVersion(templateVersionName);
      System.out.printf("Deleted template version: %s\n", templateVersionName);
    }
  }
}
// [END parametermanager_delete_param_template_version]
