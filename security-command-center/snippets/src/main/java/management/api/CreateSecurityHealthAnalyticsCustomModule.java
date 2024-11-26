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

// [START securitycenter_create_security_health_analytics_custom_module]
import com.google.cloud.securitycentermanagement.v1.CreateSecurityHealthAnalyticsCustomModuleRequest;
import com.google.cloud.securitycentermanagement.v1.CustomConfig;
import com.google.cloud.securitycentermanagement.v1.CustomConfig.ResourceSelector;
import com.google.cloud.securitycentermanagement.v1.CustomConfig.Severity;
import com.google.cloud.securitycentermanagement.v1.SecurityCenterManagementClient;
import com.google.cloud.securitycentermanagement.v1.SecurityHealthAnalyticsCustomModule;
import com.google.cloud.securitycentermanagement.v1.SecurityHealthAnalyticsCustomModule.EnablementState;
import com.google.type.Expr;
import java.io.IOException;

public class CreateSecurityHealthAnalyticsCustomModule {

  public static void main(String[] args) throws IOException {
    // https://cloud.google.com/security-command-center/docs/reference/security-center-management/rest/v1/organizations.locations.securityHealthAnalyticsCustomModules/create
    // TODO: Developer should replace project_id with a real project ID before running this code
    String projectId = "project_id";

    String customModuleDisplayName = "custom_module_display_name";

    createSecurityHealthAnalyticsCustomModule(projectId, customModuleDisplayName);
  }

  public static SecurityHealthAnalyticsCustomModule createSecurityHealthAnalyticsCustomModule(
      String projectId, String customModuleDisplayName) throws IOException {

    // Initialize client that will be used to send requests. This client only needs
    // to be created
    // once, and can be reused for multiple requests.
    try (SecurityCenterManagementClient client = SecurityCenterManagementClient.create()) {

      String name =
          String.format(
              "projects/%s/locations/global/securityHealthAnalyticsCustomModules/%s",
              projectId, "custom_module");

      // define the CEL expression here, change it according to the your requirements
      Expr expr =
          Expr.newBuilder()
              .setExpression(
                  "has(resource.rotationPeriod) && (resource.rotationPeriod > "
                      + "duration('2592000s'))")
              .build();

      // define the resource selector
      ResourceSelector resourceSelector =
          ResourceSelector.newBuilder()
              .addResourceTypes("cloudkms.googleapis.com/CryptoKey")
              .build();

      // define the custom module configuration, update the severity, description,
      // recommendation below
      CustomConfig customConfig =
          CustomConfig.newBuilder()
              .setPredicate(expr)
              .setResourceSelector(resourceSelector)
              .setSeverity(Severity.MEDIUM)
              .setDescription("add your description here")
              .setRecommendation("add your recommendation here")
              .build();

      // define the security health analytics custom module configuration, update the
      // EnablementState below
      SecurityHealthAnalyticsCustomModule securityHealthAnalyticsCustomModule =
          SecurityHealthAnalyticsCustomModule.newBuilder()
              .setName(name)
              .setDisplayName(customModuleDisplayName)
              .setEnablementState(EnablementState.ENABLED)
              .setCustomConfig(customConfig)
              .build();

      CreateSecurityHealthAnalyticsCustomModuleRequest request =
          CreateSecurityHealthAnalyticsCustomModuleRequest.newBuilder()
              .setParent(String.format("projects/%s/locations/global", projectId))
              .setSecurityHealthAnalyticsCustomModule(securityHealthAnalyticsCustomModule)
              .build();

      SecurityHealthAnalyticsCustomModule response =
          client.createSecurityHealthAnalyticsCustomModule(request);

      return response;
    }
  }
}
// [END securitycenter_create_security_health_analytics_custom_module]
