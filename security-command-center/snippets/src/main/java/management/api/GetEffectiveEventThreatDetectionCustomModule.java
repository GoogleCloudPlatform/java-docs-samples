/*
 * Copyright 2024 Google LLC
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

// [START securitycenter_get_effective_event_threat_detection_custom_module]
import com.google.cloud.securitycentermanagement.v1.EffectiveEventThreatDetectionCustomModule;
import com.google.cloud.securitycentermanagement.v1.GetEffectiveEventThreatDetectionCustomModuleRequest;
import com.google.cloud.securitycentermanagement.v1.SecurityCenterManagementClient;
import java.io.IOException;

public class GetEffectiveEventThreatDetectionCustomModule {

  public static void main(String[] args) throws IOException {
    // https://cloud.google.com/security-command-center/docs/reference/security-center-management/rest/v1/organizations.locations.effectiveEventThreatDetectionCustomModules/get
    // TODO: Developer should replace project_id with a real project ID before running this code
    String projectId = "project_id";

    String customModuleId = "custom_module_id";

    getEffectiveEventThreatDetectionCustomModule(projectId, customModuleId);
  }

  public static EffectiveEventThreatDetectionCustomModule
      getEffectiveEventThreatDetectionCustomModule(String projectId, String customModuleId)
          throws IOException {

    // Initialize client that will be used to send requests. This client only needs
    // to be created
    // once, and can be reused for multiple requests.
    try (SecurityCenterManagementClient client = SecurityCenterManagementClient.create()) {

      String qualifiedModuleName =
          String.format(
              "projects/%s/locations/global/effectiveEventThreatDetectionCustomModules/%s",
              projectId, customModuleId);

      GetEffectiveEventThreatDetectionCustomModuleRequest request =
          GetEffectiveEventThreatDetectionCustomModuleRequest.newBuilder()
              .setName(qualifiedModuleName)
              .build();

      EffectiveEventThreatDetectionCustomModule response =
          client.getEffectiveEventThreatDetectionCustomModule(request);

      return response;
    }
  }
}
// [END securitycenter_get_effective_event_threat_detection_custom_module]
