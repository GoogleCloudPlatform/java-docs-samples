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

package vtwo.marks;

// [START securitycenter_list_filtered_marks_v2]

import com.google.cloud.securitycenter.v2.ListFindingsRequest;
import com.google.cloud.securitycenter.v2.ListFindingsResponse.ListFindingsResult;
import com.google.cloud.securitycenter.v2.SecurityCenterClient;
import java.io.IOException;

public class ListMarksWithFilter {

  public static void main(String[] args) throws IOException {
    // parentId: must be in one of the following formats:
    //    "organizations/{organization_id}"
    //    "projects/{project_id}"
    //    "folders/{folder_id}"
    String organizationId = "{google-cloud-organization-id}";
    // Specify the source-id.
    String sourceId = "{source-id}";
    // Specify the location of the notification config.
    String location = "global";

    listFindingsWithQueryMarks(organizationId, sourceId, location);
  }

  // How to find the security mark writer, securitycenter.findingSecurityMarksWriter
  public static Iterable<ListFindingsResult> listFindingsWithQueryMarks(String organizationId,
      String sourceId, String location) throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (SecurityCenterClient client = SecurityCenterClient.create()) {
      // Start setting up a request to list all findings filtered by a specific security mark.
      // Use any one of the following formats:
      //  * organizations/{organization_id}/sources/{source_id}/locations/{location}
      //  * folders/{folder_id}/sources/{source_id}/locations/{location}
      //  * projects/{project_id}/sources/{source_id}/locations/{location}
        String parent = String.format("organizations/%s/sources/%s/locations/%s", organizationId,
            sourceId,
            location);

        // Listing all findings of category "MEDIUM_RISK_ONE".
        String filter = "NOT security_marks.marks.key_a=\"value_a\"";

        ListFindingsRequest request =
            ListFindingsRequest.newBuilder()
                .setParent(parent)
                .setFilter(filter)
                .build();
      Iterable<ListFindingsResult> result = client.listFindings(request).iterateAll();
      return result;
    } catch (IOException e) {
      throw new RuntimeException("Couldn't create client.", e);
    }
  }
}
// [END securitycenter_list_filtered_marks_v2]
