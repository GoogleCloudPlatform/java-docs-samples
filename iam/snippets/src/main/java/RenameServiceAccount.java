/* Copyright 2019 Google LLC
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

// [START iam_rename_service_account]

import com.google.cloud.iam.admin.v1.IAMClient;
import com.google.iam.admin.v1.GetServiceAccountRequest;
import com.google.iam.admin.v1.PatchServiceAccountRequest;
import com.google.iam.admin.v1.ServiceAccount;
import com.google.iam.admin.v1.ServiceAccountName;
import com.google.protobuf.FieldMask;
import java.io.IOException;

public class RenameServiceAccount {
  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace the variables before running the sample.
    String projectId = "your-project-id";
    String serviceAccountName = "my-service-account-name";
    String displayName = "your-new-display-name";

    renameServiceAccount(projectId, serviceAccountName, displayName);
  }

  // Changes a service account's display name.
  public static ServiceAccount renameServiceAccount(String projectId, String serviceAccountName,
                                                    String displayName) throws IOException {
    // Construct the service account email.
    // You can modify the ".iam.gserviceaccount.com" to match the service account name in which
    // you want to delete the key.
    // See, https://cloud.google.com/iam/docs/creating-managing-service-account-keys?hl=en#deleting
    String serviceAccountEmail = serviceAccountName + "@" + projectId + ".iam.gserviceaccount.com";

    // Initialize client that will be used to send requests.
    // This client only needs to be created once, and can be reused for multiple requests.
    try (IAMClient iamClient = IAMClient.create()) {
      // First, get a service account using getServiceAccount or listServiceAccounts
      GetServiceAccountRequest serviceAccountRequest = GetServiceAccountRequest.newBuilder()
              .setName(ServiceAccountName.of(projectId, serviceAccountEmail).toString())
              .build();
      ServiceAccount serviceAccount = iamClient.getServiceAccount(serviceAccountRequest);

      // You can patch only the `display_name` and `description` fields. You must use
      //  the `update_mask` field to specify which of these fields you want to patch.
      serviceAccount = serviceAccount.toBuilder().setDisplayName(displayName).build();
      PatchServiceAccountRequest patchServiceAccountRequest =
              PatchServiceAccountRequest.newBuilder()
                      .setServiceAccount(serviceAccount)
                      .setUpdateMask(FieldMask.newBuilder().addPaths("display_name").build())
                      .build();
      serviceAccount = iamClient.patchServiceAccount(patchServiceAccountRequest);

      System.out.println(
              "Updated display name for "
                      + serviceAccount.getName()
                      + " to: "
                      + serviceAccount.getDisplayName());
      return serviceAccount;
    }
  }
}
// [END iam_rename_service_account]
