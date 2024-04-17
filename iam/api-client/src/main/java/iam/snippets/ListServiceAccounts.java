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

// [START iam_list_service_accounts]

import com.google.cloud.iam.admin.v1.IAMClient;
import java.io.IOException;

public class ListServiceAccounts {

  // Lists all service accounts for the current project.
  public static void listServiceAccounts(String projectId) {
    // String projectId = "my-project-id"

    try (IAMClient iamClient = IAMClient.create()) {
      iamClient.listServiceAccounts("projects/" + projectId).getPage()
              .streamValues().forEach(account -> {
                System.out.println("Name: " + account.getName());
                System.out.println("Display name: " + account.getDisplayName());
                System.out.println("Email: " + account.getEmail() + "\n");
                System.out.println();
              });
    } catch (IOException ex) {
      System.out.println("Unable to find service accounts");
    }
  }
}
// [END iam_list_service_accounts]
