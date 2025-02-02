/*
 * Copyright 2025 Google LLC
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

// [START securitycenter_add_security_marks_assets_v2]

package vtwo.assets;

import autovalue.shaded.com.google.common.collect.ImmutableMap;
import com.google.cloud.securitycenter.v2.SecurityCenterClient;
import com.google.cloud.securitycenter.v2.SecurityMarks;
import com.google.cloud.securitycenter.v2.UpdateSecurityMarksRequest;
import com.google.protobuf.FieldMask;
import java.io.IOException;

public class AddSecurityMarksToAssets {

  public static void main(String[] args) throws IOException {
    // organizationId: Google Cloud Organization id.
    String organizationId = "ORGANIZATION_ID";

    // Specify the asset id.
    String assetId = "ASSET_ID";

    addToAsset(organizationId, assetId);
  }

  public static SecurityMarks addToAsset(String organizationId, String assetId) throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (SecurityCenterClient client = SecurityCenterClient.create()) {

      // Specify the value of 'assetName' in one of the following formats:
      // String assetName = "organizations/{org-id}/assets/{asset-id}";
      String assetName = String.format("organizations/%s/assets/%s", organizationId, assetId);

      // Start setting up a request to add security marks for a finding.
      ImmutableMap markMap = ImmutableMap.of("key_a", "value_a", "key_b", "value_b");

      // Add security marks and field mask for security marks.
      SecurityMarks securityMarks =
          SecurityMarks.newBuilder()
              .setName(assetName + "/securityMarks")
              .putAllMarks(markMap)
              .build();

      // Set the update mask to specify which properties should be updated.
      // If empty, all mutable fields will be updated.
      // For more info on constructing field mask path, see the proto or:
      // https://cloud.google.com/java/docs/reference/protobuf/latest/com.google.protobuf.FieldMask
      FieldMask updateMask =
          FieldMask.newBuilder().addPaths("marks.key_a").addPaths("marks.key_b").build();

      UpdateSecurityMarksRequest request =
          UpdateSecurityMarksRequest.newBuilder()
              .setSecurityMarks(securityMarks)
              .setUpdateMask(updateMask)
              .build();

      // Call the API and return the response.
      SecurityMarks response = client.updateSecurityMarks(request);
      return response;
    }
  }
}

// [END securitycenter_add_security_marks_assets_v2]
