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

// [START securitycenter_simulate_security_health_analytics_custom_module]
import com.google.cloud.securitycentermanagement.v1.CustomConfig;
import com.google.cloud.securitycentermanagement.v1.CustomConfig.ResourceSelector;
import com.google.cloud.securitycentermanagement.v1.CustomConfig.Severity;
import com.google.cloud.securitycentermanagement.v1.SecurityCenterManagementClient;
import com.google.cloud.securitycentermanagement.v1.SimulateSecurityHealthAnalyticsCustomModuleRequest;
import com.google.cloud.securitycentermanagement.v1.SimulateSecurityHealthAnalyticsCustomModuleRequest.SimulatedResource;
import com.google.cloud.securitycentermanagement.v1.SimulateSecurityHealthAnalyticsCustomModuleResponse;
import com.google.iam.v1.Binding;
import com.google.iam.v1.Policy;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import com.google.type.Expr;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SimulateSecurityHealthAnalyticsCustomModule {

  public static void main(String[] args) throws IOException {
    // https://cloud.google.com/security-command-center/docs/reference/security-center-management/rest/v1/organizations.locations.securityHealthAnalyticsCustomModules/simulate
    // TODO: Developer should replace project_id with a real project ID before running this code
    String projectId = "project_id";

    simulateSecurityHealthAnalyticsCustomModule(projectId);
  }

  public static SimulateSecurityHealthAnalyticsCustomModuleResponse
      simulateSecurityHealthAnalyticsCustomModule(String projectId) throws IOException {

    // Initialize client that will be used to send requests. This client only needs
    // to be created
    // once, and can be reused for multiple requests.
    try (SecurityCenterManagementClient client = SecurityCenterManagementClient.create()) {

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

      // define the simulated resource data
      Map<String, Value> resourceData = new HashMap<>();
      resourceData.put("resourceId", Value.newBuilder().setStringValue("test-resource-id").build());
      resourceData.put("name", Value.newBuilder().setStringValue("test-resource-name").build());
      Struct resourceDataStruct = Struct.newBuilder().putAllFields(resourceData).build();

      // define the policy
      Policy policy =
          Policy.newBuilder()
              .addBindings(
                  Binding.newBuilder()
                      .setRole("roles/owner")
                      .addMembers("user:test-user@gmail.com")
                      .build())
              .build();

      // replace with the correct resource type
      SimulatedResource simulatedResource =
          SimulatedResource.newBuilder()
              .setResourceType("cloudkms.googleapis.com/CryptoKey")
              .setResourceData(resourceDataStruct)
              .setIamPolicyData(policy)
              .build();

      SimulateSecurityHealthAnalyticsCustomModuleRequest request =
          SimulateSecurityHealthAnalyticsCustomModuleRequest.newBuilder()
              .setParent(String.format("projects/%s/locations/global", projectId))
              .setCustomConfig(customConfig)
              .setResource(simulatedResource)
              .build();

      SimulateSecurityHealthAnalyticsCustomModuleResponse response =
          client.simulateSecurityHealthAnalyticsCustomModule(request);

      return response;
    }
  }
}
// [END securitycenter_simulate_security_health_analytics_custom_module]
