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

package iam.snippets;

// [START iam_disable_service_account]
import com.google.cloud.iam.admin.v1.IAMClient;
import com.google.iam.admin.v1.DisableServiceAccountRequest;
import java.io.IOException;

public class DisableServiceAccount {

  // Disables a service account.
  public static void disableServiceAccount(String projectId, String serviceAccountName) {
    // String projectId = "my-project-id";
    // String serviceAccountName = "my-service-account-name";

    String serviceAccountEmail = serviceAccountName + "@" + projectId + ".iam.gserviceaccount.com";
    try (IAMClient iamClient = IAMClient.create()) {
      iamClient.disableServiceAccount(DisableServiceAccountRequest.newBuilder()
              .setName("projects/" + projectId + "/serviceAccounts/" + serviceAccountEmail)
              .build());

      System.out.println("Disabled service account: " + serviceAccountName);
    } catch (IOException ex) {
      System.out.println("Unable to disable service account");
    }
  }
}
// [END iam_disable_service_account]
