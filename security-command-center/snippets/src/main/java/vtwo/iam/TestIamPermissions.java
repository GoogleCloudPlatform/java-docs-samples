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

package vtwo.iam;

// [START securitycenter_test_iam_permissions_v2]

import com.google.cloud.securitycenter.v2.SecurityCenterClient;
import com.google.cloud.securitycenter.v2.SourceName;
import com.google.iam.v1.TestIamPermissionsResponse;
// [END securitycenter_test_iam_permissions_v2]
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TestIamPermissions {

  public static void main(String[] args) throws IOException {
    // TODO: Replace the sample resource name
    // organizationId: Google Cloud Organization id.
    String organizationId = "{google-cloud-organization-id}";

    // The source id corresponding to the finding.
    String sourceId = "{source-id}";

    // The permission. For more information see [IAM Overview].
    // https://cloud.google.com/iam/docs/overview#permissions.
    String permission = "{permission}";

    testIamPermissions(organizationId, sourceId, permission);
  }

  // [START securitycenter_test_iam_permissions_v2]
  // Demonstrates how to verify IAM permissions to create findings.
  public static TestIamPermissionsResponse testIamPermissions(String organizationId,
      String sourceId, String permission) {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources.
    try (SecurityCenterClient client = SecurityCenterClient.create()) {

      // Start setting up a request to get IAM policy for a source.
      SourceName sourceName = SourceName.of(organizationId, sourceId);

      // Iam permission to test.
      List<String> permissionsToTest = new ArrayList<>();
      permissionsToTest.add(permission);

      // Call the API.
      TestIamPermissionsResponse response = client.testIamPermissions(
          sourceName.toString(), permissionsToTest);
      return response;
    } catch (IOException e) {
      throw new RuntimeException("Couldn't create client.", e);
    }
  }
  // [END securitycenter_test_iam_permissions_v2]
}

