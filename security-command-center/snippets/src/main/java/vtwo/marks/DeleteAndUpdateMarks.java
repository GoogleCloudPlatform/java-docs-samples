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

// [START securitycenter_add_delete_security_marks_v2]

import com.google.cloud.securitycenter.v2.FindingName;
import com.google.cloud.securitycenter.v2.SecurityCenterClient;
import com.google.cloud.securitycenter.v2.SecurityMarks;
import com.google.cloud.securitycenter.v2.UpdateSecurityMarksRequest;
import com.google.protobuf.FieldMask;
import java.io.IOException;

public class DeleteAndUpdateMarks {

  public static void main(String[] args) throws IOException {
    // TODO: Replace the sample resource name
    // organizationId: Google Cloud Organization id.
    String organizationId = "{google-cloud-organization-id}";

    // Specify the source id.
    String sourceId = "{source-id}";

    // Specify the finding id.
    String findingId = "{finding-id}";

    // Specify the location.
    String location = "global";

    deleteAndUpdateMarks(organizationId, sourceId, location, findingId);
  }

  // Demonstrates updating and deleting security marks in the same request.
  public static SecurityMarks deleteAndUpdateMarks(String organizationId, String sourceId,
      String location, String findingId) throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    SecurityCenterClient client = SecurityCenterClient.create();

    // Instead of using the FindingName, a plain String can also be used. E.g.:
    // String findingName = String.format("organizations/%s/sources/%s/locations/%s/findings/%s",
    // organizationId, sourceId, location, findingId);
    // Start setting up a request to clear security marks for an asset.
    // Create security mark and field mask for clearing security marks.
    FindingName findingName = FindingName
        .ofOrganizationSourceLocationFindingName(organizationId, sourceId, location, findingId);

    SecurityMarks securityMarks =
        SecurityMarks.newBuilder()
            .setName(findingName + "/securityMarks")
            .putMarks("key_a", "new_value_for_a")
            .build();

    // Set the update mask to specify which properties should be updated.
    // If empty, all mutable fields will be updated.
    // For more info on constructing field mask path, see the proto or:
    // https://cloud.google.com/java/docs/reference/protobuf/latest/com.google.protobuf.FieldMask
    FieldMask updateMask =
        FieldMask.newBuilder()
            .addPaths("marks.key_a")
            // Since no marks have been added, including "marks.key_b" in the update mask
            // will cause it to be deleted.
            .addPaths("marks.key_b")
            .build();

    UpdateSecurityMarksRequest request =
        UpdateSecurityMarksRequest.newBuilder()
            .setSecurityMarks(securityMarks)
            .setUpdateMask(updateMask)
            .build();

    // Call the API.
    SecurityMarks response = client.updateSecurityMarks(request);

    System.out.println("Security Marks updated and cleared:" + response);
    return response;
  }
}
// [END securitycenter_add_delete_security_marks_v2]
