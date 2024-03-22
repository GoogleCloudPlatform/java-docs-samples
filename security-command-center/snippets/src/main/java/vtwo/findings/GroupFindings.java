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

// [START securitycenter_group_all_findings_v2]

import com.google.cloud.securitycenter.v2.GroupFindingsRequest;
import com.google.cloud.securitycenter.v2.GroupResult;
import com.google.cloud.securitycenter.v2.SecurityCenterClient;
import java.io.IOException;

public class GroupFindings {

  public static void main(String[] args) throws IOException {
    // TODO: Replace the variables within {}
    // organizationId: Google Cloud Organization id.
    String organizationId = "google-cloud-organization-id";

    // Specify the location to scope the findings to.
    String location = "global";

    // The source id corresponding to the finding.
    String sourceId = "source-id";

    groupFindings(organizationId, sourceId, location);
  }

  // Group all findings under a parent type across all sources by their specified properties
  // (e.g category, state).
  public static void groupFindings(String organizationId, String sourceId, String location)
      throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (SecurityCenterClient client = SecurityCenterClient.create()) {
      // Use any one of the following formats:
      //  * organizations/{organization_id}/sources/{source_id}/locations/{location}
      //  * folders/{folder_id}/sources/{source_id}/locations/{location}
      //  * projects/{project_id}/sources/{source_id}/locations/{location}
      String parent = String.format("organizations/%s/sources/%s/locations/%s",
          organizationId,
          sourceId,
          location);

      GroupFindingsRequest request =
          GroupFindingsRequest.newBuilder()
              .setParent(parent)
              // Supported grouping properties: resource_name/ category/ state/ parent/ severity.
              // Multiple properties should be separated by comma.
              .setGroupBy("category, state")
              .build();

      for (GroupResult result : client.groupFindings(request).iterateAll()) {
        System.out.println(result.getPropertiesMap());
      }
      System.out.println("Listed grouped findings.");
    }
  }
}
// [END securitycenter_group_all_findings_v2]
