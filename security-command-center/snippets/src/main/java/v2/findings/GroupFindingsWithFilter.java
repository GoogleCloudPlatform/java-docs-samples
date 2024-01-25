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

package v2.findings;

// [START securitycenter_group_filtered_findings_v2]

import com.google.cloud.securitycenter.v2.GroupFindingsRequest;
import com.google.cloud.securitycenter.v2.GroupResult;
import com.google.cloud.securitycenter.v2.SecurityCenterClient;
import java.io.IOException;

public class GroupFindingsWithFilter {

  public static void main(String[] args) throws IOException {
    // TODO: Replace the variables within {}
    // projectId: Google Cloud Project id.
    String projectId = "google-cloud-project-id";

    // Specify the DRZ location to scope the findings specific to the location.
    // Available locations: "us", "eu", "global".
    String location = "global";

    // The source id corresponding to the finding.
    String sourceId = "source-id";

    groupFilteredFindings(projectId, sourceId, location);
  }

  // Group filtered findings under a parent type across all sources by their specified properties
  // (e.g. category, state).
  public static void groupFilteredFindings(String projectId, String sourceId, String location)
      throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (SecurityCenterClient client = SecurityCenterClient.create()) {
      // Use any one of the following formats:
      //  * organizations/{organization_id}/sources/{source_id}/locations/{location}/findings
      //  * folders/{folder_id}/sources/{source_id}/locations/{location}/findings
      //  * projects/{project_id}/sources/{source_id}/locations/{location}/findings
      String parent = String.format("projects/%s/sources/%s/locations/%s/findings", projectId,
          sourceId,
          location);

      // Group all findings of category "MEDIUM_RISK_ONE".
      String filter = "category=\"MEDIUM_RISK_ONE\"";

      GroupFindingsRequest request =
          GroupFindingsRequest.newBuilder()
              .setParent(parent)
              // Supported grouping properties: resource_name/ category/ state/ parent/ severity.
              // Multiple properties should be separated by comma.
              .setGroupBy("state")
              .setFilter(filter)
              .build();

      for (GroupResult result : client.groupFindings(request).iterateAll()) {
        System.out.println(result.getPropertiesMap());
      }
      System.out.println("Listed filtered and grouped findings.");
    }
  }
}
// [END securitycenter_group_filtered_findings_v2]
