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

// [START securitycenter_update_security_center_service]
import com.google.cloud.securitycentermanagement.v1.SecurityCenterManagementClient;
import com.google.cloud.securitycentermanagement.v1.SecurityCenterService;
import com.google.cloud.securitycentermanagement.v1.SecurityCenterService.EnablementState;
import com.google.cloud.securitycentermanagement.v1.UpdateSecurityCenterServiceRequest;
import com.google.protobuf.FieldMask;
import java.io.IOException;

public class UpdateSecurityCenterService {

  public static void main(String[] args) throws IOException {
    // https://cloud.google.com/security-command-center/docs/reference/security-center-management/rest/v1/organizations.locations.securityCenterServices/patch
    // TODO: Replace <project-id> with your project ID
    String projectId = "<project_id>";
    // Replace service with one of the valid values:
    // container-threat-detection, event-threat-detection, security-health-analytics,
    // vm-threat-detection, web-security-scanner
    String service = "<service>";

    updateSecurityCenterService(projectId, service);
  }

  public static SecurityCenterService updateSecurityCenterService(String projectId, String service)
      throws IOException {
    // Initialize client that will be used to send requests. This client only needs
    // to be created
    // once, and can be reused for multiple requests.
    try (SecurityCenterManagementClient client = SecurityCenterManagementClient.create()) {
      String name =
          String.format(
              "projects/%s/locations/global/securityCenterServices/%s", projectId, service);
      // Define the security center service configuration, update the
      // IntendedEnablementState accordingly.
      SecurityCenterService securityCenterService =
          SecurityCenterService.newBuilder()
              .setName(name)
              .setIntendedEnablementState(EnablementState.ENABLED)
              .build();
      // Set the field mask to specify which properties should be updated.
      FieldMask fieldMask = FieldMask.newBuilder().addPaths("intended_enablement_state").build();
      UpdateSecurityCenterServiceRequest request =
          UpdateSecurityCenterServiceRequest.newBuilder()
              .setSecurityCenterService(securityCenterService)
              .setUpdateMask(fieldMask)
              .build();
      SecurityCenterService response = client.updateSecurityCenterService(request);
      return response;
    }
  }
}
// [END securitycenter_update_security_center_service]
