/*
 * Copyright 2024 Google LLC
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

package vtwo.muteconfig;

// [START securitycenter_update_mute_config_v2]

import com.google.cloud.securitycenter.v2.MuteConfig;
import com.google.cloud.securitycenter.v2.SecurityCenterClient;
import com.google.cloud.securitycenter.v2.UpdateMuteConfigRequest;
import com.google.protobuf.FieldMask;
import java.io.IOException;

public class UpdateMuteRule {

  public static void main(String[] args) throws IOException {
    // TODO: Replace the variables within {}
    // projectId: Google Cloud Project id.
    String projectId = "google-cloud-project-id";

    // Specify the location of the mute config to update. If the mute config was
    // created with v1 API, it can be accessed with "global".
    String location = "global";

    // muteConfigId: Name of the mute config to update.
    String muteConfigId = "mute-config-id";

    updateMuteRule(projectId, location, muteConfigId);
  }

  // Updates an existing mute configuration.
  // The following can be updated in a mute config: description and filter.
  public static void updateMuteRule(String projectId, String location, String muteConfigId)
      throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (SecurityCenterClient securityCenterClient = SecurityCenterClient.create()) {

      MuteConfig updateMuteConfig =
          MuteConfig.newBuilder()
              // Construct the name according to the parent type of the mute rule.
              // Parent can also be one of:
              //  * "organizations/{org_id}/locations/{location}/muteConfigs/{muteConfig_id}"
              //  * "folders/{folder_id}/locations/{location}/muteConfigs/{muteConfig_id}"
              .setName(String.format("projects/%s/locations/%s/muteConfigs/%s", projectId, location,
                  muteConfigId))
              .setDescription("Updated mute config description")
              .build();

      UpdateMuteConfigRequest updateMuteConfigRequest =
          UpdateMuteConfigRequest.newBuilder()
              .setMuteConfig(updateMuteConfig)
              // Make sure that the mask fields match the properties changed in
              // 'updateMuteConfig' object.
              // For more info on constructing update mask path, see the proto or:
              // https://cloud.google.com/security-command-center/docs/reference/rest/v2/folders.muteConfigs/patch?hl=en#query-parameters
              .setUpdateMask(FieldMask.newBuilder().addPaths("description").build())
              .build();

      MuteConfig response = securityCenterClient.updateMuteConfig(updateMuteConfigRequest);
      System.out.println(response);
    }
  }
}
// [END securitycenter_update_mute_config_v2]
