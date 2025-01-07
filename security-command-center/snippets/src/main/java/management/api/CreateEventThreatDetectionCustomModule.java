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

// [START securitycenter_create_event_threat_detection_custom_module]
import com.google.cloud.securitycentermanagement.v1.CreateEventThreatDetectionCustomModuleRequest;
import com.google.cloud.securitycentermanagement.v1.EventThreatDetectionCustomModule;
import com.google.cloud.securitycentermanagement.v1.EventThreatDetectionCustomModule.EnablementState;
import com.google.cloud.securitycentermanagement.v1.SecurityCenterManagementClient;
import com.google.protobuf.ListValue;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateEventThreatDetectionCustomModule {

  public static void main(String[] args) throws IOException {
    // https://cloud.google.com/security-command-center/docs/reference/security-center-management/rest/v1/organizations.locations.eventThreatDetectionCustomModules/create
    // TODO: Developer should replace project_id with a real project ID before running this code
    String projectId = "project_id";

    String customModuleDisplayName = "custom_module_display_name";

    createEventThreatDetectionCustomModule(projectId, customModuleDisplayName);
  }

  public static EventThreatDetectionCustomModule createEventThreatDetectionCustomModule(
      String projectId, String customModuleDisplayName) throws IOException {

    // Initialize client that will be used to send requests. This client only needs
    // to be created
    // once, and can be reused for multiple requests.
    try (SecurityCenterManagementClient client = SecurityCenterManagementClient.create()) {

    	String parent = String.format("projects/%s/locations/global", projectId);
    	
      // define the metadata and other config parameters severity, description,
      // recommendation and ips below
      Map<String, Value> metadata = new HashMap<>();
      metadata.put("severity", Value.newBuilder().setStringValue("MEDIUM").build());
      metadata.put(
          "description", Value.newBuilder().setStringValue("add your description here").build());
      metadata.put(
          "recommendation",
          Value.newBuilder().setStringValue("add your recommendation here").build());
      List<Value> ips = Arrays.asList(Value.newBuilder().setStringValue("0.0.0.0").build());
      
      Value metadataVal =
          Value.newBuilder()
              .setStructValue(Struct.newBuilder().putAllFields(metadata).build())
              .build();
      Value ipsValue =
          Value.newBuilder().setListValue(ListValue.newBuilder().addAllValues(ips).build()).build();

      Struct configStruct =
          Struct.newBuilder().putFields("metadata", metadataVal).putFields("ips", ipsValue).build();

      // define the Event Threat Detection custom module configuration, update the EnablementState
      // below
      EventThreatDetectionCustomModule eventThreatDetectionCustomModule =
          EventThreatDetectionCustomModule.newBuilder()
              .setConfig(configStruct)
              .setDisplayName(customModuleDisplayName)
              .setEnablementState(EnablementState.ENABLED)
              .setType("CONFIGURABLE_BAD_IP")
              .build();

      CreateEventThreatDetectionCustomModuleRequest request =
          CreateEventThreatDetectionCustomModuleRequest.newBuilder()
              .setParent(parent)
              .setEventThreatDetectionCustomModule(eventThreatDetectionCustomModule)
              .build();

      EventThreatDetectionCustomModule response =
          client.createEventThreatDetectionCustomModule(request);

      return response;
    }
  }
}
// [END securitycenter_create_event_threat_detection_custom_module]
