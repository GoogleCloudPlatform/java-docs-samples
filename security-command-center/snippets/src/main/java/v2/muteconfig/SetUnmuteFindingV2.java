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

package v2.muteconfig;

// [START securitycenter_set_unmute_v2]

import com.google.cloud.securitycenter.v2.Finding;
import com.google.cloud.securitycenter.v2.Finding.Mute;
import com.google.cloud.securitycenter.v2.SecurityCenterClient;
import com.google.cloud.securitycenter.v2.SetMuteRequest;
import java.io.IOException;

public class SetUnmuteFindingV2 {

  public static void main(String[] args) {
    // TODO: Replace the variables within {}
    // projectId: Google Cloud Project id.
    String projectId = "google-cloud-project-id";

    // Specify the DRZ location of the mute config. If the mute config was
    // created with v1 API, it can be accessed with "global".
    // Available locations: "us", "eu", "global".
    String location = "global";

    // The source id corresponding to the finding.
    String sourceId = "source-id";

    String findingId = "finding-id";

    setUnmute(projectId, location, sourceId, findingId);
  }

  // Unmute an individual finding.
  // Unmuting a finding that isn't muted has no effect.
  // Various mute states are: MUTE_UNSPECIFIED/MUTE/UNMUTE.
  public static void setUnmute(String projectId, String location, String sourceId, String findingId) {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (SecurityCenterClient client = SecurityCenterClient.create()) {

      // Create the finding path. See:
      // https://cloud.google.com/apis/design/resource_names#relative_resource_name
      // Use any one of the following formats:
      //  * organizations/{organization_id}/sources/{source_id}/locations/{location}/finding/{finding_id}
      //  * folders/{folder_id}/sources/{source_id}/locations/{location}/finding/{finding_id}
      //  * projects/{project_id}/sources/{source_id}/locations/{location}/finding/{finding_id}
      String findingPath = String.format("projects/%s/sources/%s/locations/%s/findings/%s",
          projectId, sourceId, location, findingId);

      SetMuteRequest setMuteRequest =
          SetMuteRequest.newBuilder()
              .setName(findingPath)
              .setMute(Mute.UNMUTED)
              .build();

      Finding finding = client.setMute(setMuteRequest);
      System.out.println(
          "Mute value for the finding " + finding.getName() + " is: " + finding.getMute());
    } catch (IOException e) {
      System.out.println("Failed to set the specified mute value. \n Exception: " + e);
    }
  }
}
// [END securitycenter_set_unmute_v2]
