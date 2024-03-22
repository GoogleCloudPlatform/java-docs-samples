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

// [START securitycenter_delete_mute_config_v2]

import com.google.cloud.securitycenter.v2.MuteConfigName;
import com.google.cloud.securitycenter.v2.SecurityCenterClient;
import java.io.IOException;

public class DeleteMuteRule {

  public static void main(String[] args) throws IOException {
    // TODO(Developer): Replace the following variables
    // projectId: Google Cloud Project id.
    String projectId = "google-cloud-project-id";

    // Specify the location of the mute config. If the mute config was
    // created with v1 API, it can be accessed with "global".
    String location = "global";

    // muteConfigId: Specify the name of the mute config to delete.
    String muteConfigId = "mute-config-id";

    deleteMuteRule(projectId, location, muteConfigId);
  }

  // Deletes a mute configuration given its resource name.
  // Note: Previously muted findings are not affected when a mute config is deleted.
  public static void deleteMuteRule(String projectId, String location, String muteConfigId)
      throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (SecurityCenterClient client = SecurityCenterClient.create()) {
      // Use appropriate `MuteConfigName` methods depending on the parent type.
      // folder -> MuteConfigName.ofFolderLocationMuteConfigName()
      // organization -> MuteConfigName.ofOrganizationLocationMuteConfigName()
      client.deleteMuteConfig(
          MuteConfigName.ofProjectLocationMuteConfigName(projectId, location, muteConfigId));

      System.out.println("Mute rule deleted successfully: " + muteConfigId);
    }
  }
}
// [END securitycenter_delete_mute_config_v2]
