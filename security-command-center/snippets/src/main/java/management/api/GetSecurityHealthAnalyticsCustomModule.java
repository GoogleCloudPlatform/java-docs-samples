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

// [START securitycenter_get_security_health_analytics_custom_module]
import com.google.cloud.securitycentermanagement.v1.GetSecurityHealthAnalyticsCustomModuleRequest;
import com.google.cloud.securitycentermanagement.v1.SecurityCenterManagementClient;
import com.google.cloud.securitycentermanagement.v1.SecurityHealthAnalyticsCustomModule;
import java.io.IOException;

public class GetSecurityHealthAnalyticsCustomModule {

  public static void main(String[] args) throws IOException {
    // https://cloud.google.com/security-command-center/docs/reference/security-center-management/rest/v1/organizations.locations.securityHealthAnalyticsCustomModules/get
    //  replace "project_id" with a real project ID
    String parent = String.format("projects/%s/locations/%s", "project_id", "global");

    String customModuleId = "custom_module_id";

    getSecurityHealthAnalyticsCustomModule(parent, customModuleId);
  }

  public static SecurityHealthAnalyticsCustomModule getSecurityHealthAnalyticsCustomModule(
      String parent, String customModuleId) throws IOException {

    // Initialize client that will be used to send requests. This client only needs
    // to be created
    // once, and can be reused for multiple requests.
    try (SecurityCenterManagementClient client = SecurityCenterManagementClient.create()) {

      String name =
          String.format("%s/securityHealthAnalyticsCustomModules/%s", parent, customModuleId);

      GetSecurityHealthAnalyticsCustomModuleRequest request =
          GetSecurityHealthAnalyticsCustomModuleRequest.newBuilder().setName(name).build();

      SecurityHealthAnalyticsCustomModule response =
          client.getSecurityHealthAnalyticsCustomModule(request);

      return response;
    }
  }
}
// [END securitycenter_get_security_health_analytics_custom_module]
