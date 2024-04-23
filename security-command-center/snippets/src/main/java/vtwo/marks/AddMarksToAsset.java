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

// [START securitycenter_add_security_marks_to_asset_v2]

import autovalue.shaded.com.google.common.collect.ImmutableMap;
import com.google.cloud.securitycenter.v2.SecurityCenterClient;
import com.google.cloud.securitycenter.v2.SecurityMarks;
import com.google.cloud.securitycenter.v2.UpdateSecurityMarksRequest;
import com.google.protobuf.FieldMask;
import java.io.IOException;

public class AddMarksToAsset {

  public static void main(String[] args) {
    // parentId: must be in one of the following formats:
    //    "organizations/{organization_id}"
    //    "projects/{project_id}"
    //    "folders/{folder_id}"
    String organizationId = "{google-cloud-organization-id}";

    // Specify the source-id.
    String sourceId = "{source-id}";

    // Specify the finding-id.
    String findingId = "{finding-id}";

    // Specify the location of the notification config.
    String location = "global";

    addMarksToAsset(organizationId, sourceId, location, findingId);
  }
  // Asset Asset Mark Writer, securitycenter.assetSecurityMarksWriter

  public static SecurityMarks addMarksToAsset(String organizationId, String sourceId,
      String location, String findingId) {
    try (SecurityCenterClient client = SecurityCenterClient.create()) {
      // Specify the value of 'assetName' in one of the following formats:
      //    String assetName = "organizations/{org-id}/assets/{asset-id}";
      //    String assetName = "projects/{project-id}/assets/{asset-id}";
      //    String assetName = "folders/{folder-id}/assets/{asset-id}";
      //
      // Start setting up a request to add security marks for an asset.
      ImmutableMap markMap = ImmutableMap.of("key_a", "value_a", "key_b", "value_b");
      String assetName = String.format(
          "organizations/%s/sources/%s/locations/%s/findings/%s", organizationId,
          sourceId, location, findingId);
      // Add security marks and field mask for security marks.
      SecurityMarks securityMarks =
          SecurityMarks.newBuilder()
              .setName(assetName+"/securityMarks")
              .putAllMarks(markMap)
              .build();

      FieldMask updateMask =
          FieldMask.newBuilder().addPaths("marks.key_a").addPaths("marks.key_b").build();

      UpdateSecurityMarksRequest request =
          UpdateSecurityMarksRequest.newBuilder()
              .setSecurityMarks(securityMarks)
              .setUpdateMask(updateMask)
              .build();

      // Call the API.
      SecurityMarks response = client.updateSecurityMarks(request);

      System.out.println("Security Marks:");
      System.out.println(response);
      return response;
    } catch (IOException e) {
      throw new RuntimeException("Couldn't create client.", e);
    }
  }
}
// [END securitycenter_add_security_marks_to_asset_v2]
