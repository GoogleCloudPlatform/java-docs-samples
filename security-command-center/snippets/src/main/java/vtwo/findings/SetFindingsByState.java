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

package vtwo.findings;

// [START securitycenter_set_findings_by_state_v2]

import com.google.cloud.securitycenter.v2.Finding;
import com.google.cloud.securitycenter.v2.Finding.State;
import com.google.cloud.securitycenter.v2.FindingName;
import com.google.cloud.securitycenter.v2.SecurityCenterClient;
import com.google.cloud.securitycenter.v2.SetFindingStateRequest;
import java.io.IOException;

public class SetFindingsByState {

  public static void main(String[] args) throws IOException {
    // organizationId: Google Cloud Organization id.
    String organizationId = "{google-cloud-organization-id}";

    // Specify the location to list the findings.
    String location = "global";

    // The source id corresponding to the finding.
    String sourceId = "{source-id}";

    // The finding id.
    String findingId = "{finding-id}";

    setFindingState(organizationId, location, sourceId, findingId);
  }

  // Demonstrates how to update a finding's state
  public static Finding setFindingState(String organizationId, String location, String sourceId,
      String findingId) throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (SecurityCenterClient client = SecurityCenterClient.create()) {
      // Optionally FindingName or String can be used.
      // String findingName = String.format("organizations/%s/sources/%s/locations/%s/findings/%s",
      // organizationId,sourceId,location,findingId);
      FindingName findingName = FindingName
          .ofOrganizationSourceLocationFindingName(organizationId, sourceId, location, findingId);

      SetFindingStateRequest request = SetFindingStateRequest.newBuilder()
          .setName(findingName.toString())
          .setState(State.INACTIVE)
          .build();

      // Call the API.
      Finding finding = client.setFindingState(request);

      System.out.println("Updated Finding: " + finding);
      return finding;
    }
  }
}
// [END securitycenter_set_findings_by_state_v2]
