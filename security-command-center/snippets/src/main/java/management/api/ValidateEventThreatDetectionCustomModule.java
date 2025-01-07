/*
 * Copyright 2025 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package management.api;

// [START securitycenter_validate_event_threat_detection_custom_module]
import com.google.cloud.securitycentermanagement.v1.SecurityCenterManagementClient;
import com.google.cloud.securitycentermanagement.v1.ValidateEventThreatDetectionCustomModuleRequest;
import com.google.cloud.securitycentermanagement.v1.ValidateEventThreatDetectionCustomModuleResponse;
import com.google.cloud.securitycentermanagement.v1.ValidateEventThreatDetectionCustomModuleResponse.CustomModuleValidationError;
import java.io.IOException;

public class ValidateEventThreatDetectionCustomModule {

  public static void main(String[] args) throws IOException {

    // https://cloud.google.com/security-command-center/docs/reference/security-center-management/rest/v1/organizations.locations.eventThreatDetectionCustomModules/validate
    // TODO: Developer should replace project_id with a real project ID before running this code
    String projectId = "project_id";

    validateEventThreatDetectionCustomModule(projectId);
  }

  public static ValidateEventThreatDetectionCustomModuleResponse
      validateEventThreatDetectionCustomModule(String projectId) throws IOException {

    // Initialize client that will be used to send requests. This client only needs
    // to be created
    // once, and can be reused for multiple requests.
    try (SecurityCenterManagementClient client = SecurityCenterManagementClient.create()) {

      String parent = String.format("projects/%s/locations/global", projectId);

      // Define the raw JSON configuration for the Event Threat Detection custom module
      String rawText = 
          "{"
              + "\"ips\": [\"192.0.2.1\"],"
              + "\"metadata\": {"
              + "  \"properties\": {"
              + "    \"someProperty\": \"someValue\""
              + "  },"
              + "  \"severity\": \"MEDIUM\""
              + "}"
              + "}";

      ValidateEventThreatDetectionCustomModuleRequest request =
          ValidateEventThreatDetectionCustomModuleRequest.newBuilder()
              .setParent(parent)
              .setRawText(rawText) // Use JSON as a string for validation
              .setType("CONFIGURABLE_BAD_IP")
              .build();

      // Perform validation
      ValidateEventThreatDetectionCustomModuleResponse response =
          client.validateEventThreatDetectionCustomModule(request);

      // Handle the response and output validation results
      if (response.getErrorsCount() > 0) {
        for (CustomModuleValidationError module : response.getErrorsList()) {
          System.out.printf(
              "FieldPath : %s, Description : %s \n",
              module.getFieldPath(), module.getDescription());
        }
      } else {
        System.out.println("Validation successful: No errors found.");
      }
      return response;
    }
  }
}
// [END securitycenter_validate_event_threat_detection_custom_module]
