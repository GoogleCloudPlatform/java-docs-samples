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

// [START securitycenter_list_findings_with_security_marks_v2]

import com.google.cloud.securitycenter.v2.Finding;
import com.google.cloud.securitycenter.v2.ListFindingsRequest;
import com.google.cloud.securitycenter.v2.ListFindingsResponse.ListFindingsResult;
import com.google.cloud.securitycenter.v2.SecurityCenterClient;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ListFindingMarksWithFilter {

  public static void main(String[] args) throws IOException {
    // TODO: Replace the sample resource name
    // organizationId: Google Cloud Organization id.
    String organizationId = "{google-cloud-organization-id}";

    // Specify the source-id.
    String sourceId = "{source-id}";

    // Specify the location.
    String location = "global";

    listFindingsWithQueryMarks(organizationId, sourceId, location);
  }

  // Demonstrates how to filter and list findings by security mark.
  public static List<Finding> listFindingsWithQueryMarks(String organizationId,
      String sourceId, String location) throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    SecurityCenterClient client = SecurityCenterClient.create();

    // Start setting up a request to list all findings filtered by a specific security mark.
    // Use any one of the following formats:
    //  * organizations/{organization_id}/sources/{source_id}/locations/{location}
    //  * folders/{folder_id}/sources/{source_id}/locations/{location}
    //  * projects/{project_id}/sources/{source_id}/locations/{location}
    String parent = String.format("organizations/%s/sources/%s/locations/%s",
        organizationId, sourceId, location);

    // Lists findings where the 'security_marks.marks.key_a' field does not equal 'value_a'.
    String filter = "NOT security_marks.marks.key_a=\"value_a\"";

    ListFindingsRequest request = ListFindingsRequest.newBuilder()
        .setParent(parent)
        .setFilter(filter)
        .build();

    // Call the API.
    List<Finding> listFindings = new ArrayList<>();
    Iterable<ListFindingsResult> resultList = client.listFindings(request).iterateAll();
    resultList.forEach(result -> listFindings.add(result.getFinding()));

    for (Finding finding : listFindings) {
      System.out.println("List findings: " + finding);
    }
    return listFindings;
  }
}
// [END securitycenter_list_findings_with_security_marks_v2]
