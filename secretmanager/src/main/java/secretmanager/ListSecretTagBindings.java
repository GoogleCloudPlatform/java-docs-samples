/*
 * Copyright 2026 Google LLC
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

package secretmanager;

// [START secretmanager_list_secret_tag_bindings]
import com.google.cloud.resourcemanager.v3.ListTagBindingsRequest;
import com.google.cloud.resourcemanager.v3.TagBinding;
import com.google.cloud.resourcemanager.v3.TagBindingsClient;
import java.io.IOException;

public class ListSecretTagBindings {

  public static void main(String[] args) throws Exception {
    // TODO(developer): replace these variables before running the sample.

    // This is the id of the GCP project
    String projectId = "your-project-id";
    // This is the id of the secret to act on
    String secretId = "your-secret-id";

    listSecretTagBindings(projectId, secretId);
  }

  // List tag bindings attached to the secret resource.
  public static void listSecretTagBindings(String projectId, String secretId)
      throws IOException {

    // Resource Manager TagBindings are listed under a parent such as the project.
    String parent = String.format("//secretmanager.googleapis.com/projects/%s/secrets/%s",
        projectId, secretId);

    try (TagBindingsClient tagBindingsClient = TagBindingsClient.create()) {
      ListTagBindingsRequest request = 
          ListTagBindingsRequest.newBuilder().setParent(parent).build();

      // Iterate over tag bindings
      for (TagBinding binding : tagBindingsClient.listTagBindings(request).iterateAll()) {
        System.out.printf("Found TagBinding with Name %s and TagValue %s\n",
            binding.getName(), binding.getTagValue());
      }
    }
  }
}
// [END secretmanager_list_secret_tag_bindings]
