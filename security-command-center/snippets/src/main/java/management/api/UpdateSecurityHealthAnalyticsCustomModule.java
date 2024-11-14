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

// [START securitycenter_update_security_health_analytics_custom_module]
import com.google.cloud.securitycentermanagement.v1.SecurityCenterManagementClient;
import com.google.cloud.securitycentermanagement.v1.SecurityHealthAnalyticsCustomModule;
import com.google.cloud.securitycentermanagement.v1.SecurityHealthAnalyticsCustomModule.EnablementState;
import com.google.cloud.securitycentermanagement.v1.UpdateSecurityHealthAnalyticsCustomModuleRequest;
import com.google.protobuf.FieldMask;
import java.io.IOException;

public class UpdateSecurityHealthAnalyticsCustomModule {

  public static void main(String[] args) throws IOException {
    // https://cloud.google.com/security-command-center/docs/reference/security-center-management/rest/v1/organizations.locations.securityHealthAnalyticsCustomModules/patch
    // TODO: Developer should replace project_id with a real project ID before running this code
    String parent = String.format("projects/%s/locations/%s", "project_id", "global");

    String customModuleId = "custom_module_id";

    updateSecurityHealthAnalyticsCustomModule(parent, customModuleId);
  }

  public static SecurityHealthAnalyticsCustomModule updateSecurityHealthAnalyticsCustomModule(
      String parent, String customModuleId) throws IOException {

    // Initialize client that will be used to send requests. This client only needs
    // to be created
    // once, and can be reused for multiple requests.
    try (SecurityCenterManagementClient client = SecurityCenterManagementClient.create()) {

      String name =
          String.format("%s/securityHealthAnalyticsCustomModules/%s", parent, customModuleId);

      // Define the security health analytics custom module configuration, update the
      // EnablementState accordingly.
      SecurityHealthAnalyticsCustomModule securityHealthAnalyticsCustomModule =
          SecurityHealthAnalyticsCustomModule.newBuilder()
              .setName(name)
              .setEnablementState(EnablementState.DISABLED)
              .build();

      // Set the field mask to specify which properties should be updated.
      FieldMask fieldMask = FieldMask.newBuilder().addPaths("enablement_state").build();

      UpdateSecurityHealthAnalyticsCustomModuleRequest request =
          UpdateSecurityHealthAnalyticsCustomModuleRequest.newBuilder()
              .setSecurityHealthAnalyticsCustomModule(securityHealthAnalyticsCustomModule)
              .setUpdateMask(fieldMask)
              .build();

      SecurityHealthAnalyticsCustomModule response =
          client.updateSecurityHealthAnalyticsCustomModule(request);

      return response;
    }
  }
}
// [END securitycenter_update_security_health_analytics_custom_module]
