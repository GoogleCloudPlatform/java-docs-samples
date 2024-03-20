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

// [START securitycenter_create_mute_config_v2]

import com.google.cloud.securitycenter.v2.LocationName;
import com.google.cloud.securitycenter.v2.MuteConfig;
import com.google.cloud.securitycenter.v2.MuteConfig.MuteConfigType;
import com.google.cloud.securitycenter.v2.SecurityCenterClient;
import java.io.IOException;
import java.util.UUID;

public class CreateMuteRule {

  public static void main(String[] args) throws IOException {
    // TODO: Replace the following variables.
    // projectId: Google Cloud Project id.
    String projectId = "google-cloud-project-id";

    // Specify the location of the mute config.
    String location = "global";

    // muteConfigId: Set a random id; max of 63 chars.
    String muteConfigId = "random-mute-id-" + UUID.randomUUID();

    createMuteRule(projectId, location, muteConfigId);
  }

  // Creates a mute configuration in a project under a given location.
  public static void createMuteRule(String projectId, String location, String muteConfigId)
      throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (SecurityCenterClient client = SecurityCenterClient.create()) {

      MuteConfig muteConfig =
          MuteConfig.newBuilder()
              .setDescription("Mute low-medium IAM grants excluding 'compute' ")
              // Set mute rule(s).
              // To construct mute rules and for supported properties, see:
              // https://cloud.google.com/security-command-center/docs/how-to-mute-findings#create_mute_rules
              .setFilter(
                  "severity=\"LOW\" OR severity=\"MEDIUM\" AND "
                      + "category=\"Persistence: IAM Anomalous Grant\" AND "
                      + "-resource.type:\"compute\"")
              .setType(MuteConfigType.STATIC)
              .build();

      // You can also create mute rules in an organization/ folder.
      // Construct the parameters according to the parent resource.
      //  * Organization -> client.createMuteConfig(OrganizationLocationName.of(...
      //  * Folder -> client.createMuteConfig(FolderLocationName.of(...
      MuteConfig response = client.createMuteConfig(
          LocationName.of(projectId, location), muteConfig, muteConfigId);
      System.out.println("Mute rule created successfully: " + response.getName());
    }
  }
}
// [END securitycenter_create_mute_config_v2]
