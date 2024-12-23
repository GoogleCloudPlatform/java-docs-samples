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

// [START securitycenter_get_security_center_service]
import com.google.cloud.securitycentermanagement.v1.GetSecurityCenterServiceRequest;
import com.google.cloud.securitycentermanagement.v1.SecurityCenterManagementClient;
import com.google.cloud.securitycentermanagement.v1.SecurityCenterService;
import java.io.IOException;

public class GetSecurityCenterService {

  public static void main(String[] args) throws IOException {
    // https://cloud.google.com/security-command-center/docs/reference/security-center-management/rest/v1/organizations.locations.securityCenterServices/get
    // TODO: Replace <project-id> with your project ID
    String projectId = "<project_id>";
    // Replace service with one of the valid values:
    // container-threat-detection, event-threat-detection, security-health-analytics,
    // vm-threat-detection, web-security-scanner
    String service = "<service>";

    getSecurityCenterService(projectId, service);
  }

  public static SecurityCenterService getSecurityCenterService(String projectId, String service)
      throws IOException {

    // Initialize client that will be used to send requests. This client only needs
    // to be created
    // once, and can be reused for multiple requests.
    try (SecurityCenterManagementClient client = SecurityCenterManagementClient.create()) {

      String name =
          String.format(
              "projects/%s/locations/global/securityCenterServices/%s", projectId, service);

      GetSecurityCenterServiceRequest request =
          GetSecurityCenterServiceRequest.newBuilder().setName(name).build();

      SecurityCenterService response = client.getSecurityCenterService(request);

      return response;
    }
  }
}
// [END securitycenter_get_security_center_service]
