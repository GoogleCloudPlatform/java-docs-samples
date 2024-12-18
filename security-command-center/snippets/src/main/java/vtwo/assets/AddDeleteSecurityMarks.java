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

package vtwo.assets;

import com.google.cloud.securitycenter.v2.SecurityCenterClient;
import com.google.cloud.securitycenter.v2.SecurityMarks;
import com.google.cloud.securitycenter.v2.UpdateSecurityMarksRequest;
import com.google.protobuf.FieldMask;
import java.io.IOException;

//[START securitycenter_add_delete_security_marks_assets_v2]

public class AddDeleteSecurityMarks {
  public static void main(String[] args) throws IOException {
    // organizationId: Google Cloud Organization id.
    String organizationId = "{google-cloud-organization-id}";

    // Specify the finding-id.
    String assetId = "{asset-id}";

    // Specify the location.
    String location = "global";

    addDeleteSecurityMarks(organizationId, location, assetId);
  }

  // Demonstrates adding/updating at the same time as deleting security
  // marks from an asset.
  // To add or change security marks, you must have an IAM role that includes permission:
  public static SecurityMarks addDeleteSecurityMarks(String organizationId,
      String location, String assetId) throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    SecurityCenterClient client = SecurityCenterClient.create();

    // Specify the value of 'assetName' in one of the following formats:
    //    String assetName = "organizations/{org-id}/assets/{asset-id}";
    //    String assetName = "projects/{project-id}/assets/{asset-id}";
    //    String assetName = "folders/{folder-id}/assets/{asset-id}";
    String assetName = String.format("organizations/%s/assets/%s", organizationId, assetId);   

    // Start setting up a request to clear and update security marks for an asset.
    // Create security mark and field mask for clearing security marks.
    SecurityMarks securityMarks = SecurityMarks.newBuilder()
        .setName(assetName + "/securityMarks")
        .putMarks("key_a", "new_value_for_a")
        .build();

    FieldMask updateMask = FieldMask.newBuilder()
        .addPaths("marks.key_a")
        .addPaths("marks.key_b")
        .build();

    UpdateSecurityMarksRequest request = UpdateSecurityMarksRequest.newBuilder()
        .setSecurityMarks(securityMarks)
        .setUpdateMask(updateMask)
        .build();

    // Call the API.
    SecurityMarks response = client.updateSecurityMarks(request);

    System.out.println("Security Marks updated and cleared::" + response);
    return response;
  }
}

//[END securitycenter_add_delete_security_marks_assets_v2]
