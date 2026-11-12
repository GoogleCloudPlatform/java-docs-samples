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

package secretmanager.regionalsamples;

// [START secretmanager_delete_regional_secret_tag]
import com.google.cloud.resourcemanager.v3.ListTagBindingsRequest;
import com.google.cloud.resourcemanager.v3.TagBinding;
import com.google.cloud.resourcemanager.v3.TagBindingsClient;
import com.google.cloud.resourcemanager.v3.TagBindingsSettings;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class DeleteRegionalSecretTag {

  public static void main(String[] args) throws Exception {
    // TODO(developer): replace these variables before running the sample.

    // This is the id of the GCP project
    String projectId = "your-project-id";
    // Location of the secret.
    String locationId = "your-location-id";
    // This is the id of the secret to act on
    String secretId = "your-secret-id";
    // Tag value to bind, e.g. "tagValues/123"
    String tagValueName = "your-tag-value";

    deleteRegionalSecretTag(projectId, locationId, secretId, tagValueName);
  }

  // Remove a TagValue from a regional Secret by deleting the TagBinding.
  public static void deleteRegionalSecretTag(
      String projectId, String locationId, String secretId, String tagValueName)
      throws IOException, InterruptedException, ExecutionException {

    String parent = String.format(
        "//secretmanager.googleapis.com/projects/%s/locations/%s/secrets/%s",
        projectId, locationId, secretId);

    // Endpoint to call the regional secret manager server
    String apiEndpoint = String.format("%s-cloudresourcemanager.googleapis.com:443", locationId);
    TagBindingsSettings tagBindingsSettings = 
        TagBindingsSettings.newBuilder().setEndpoint(apiEndpoint).build();

    try (TagBindingsClient tagBindingsClient = TagBindingsClient.create(tagBindingsSettings)) {
      ListTagBindingsRequest request = ListTagBindingsRequest.newBuilder()
          .setParent(parent).build();

      // Iterate over tag bindings
      for (TagBinding binding : tagBindingsClient.listTagBindings(request).iterateAll()) {
        // Delete the TagBinding if it matches the specified TagValue
        if (binding.getTagValue().equals(tagValueName)) {
          tagBindingsClient.deleteTagBindingAsync(binding.getName()).get();
          System.out.printf("Deleted TagBinding with Name %s and TagValue %s\n",
              binding.getName(), binding.getTagValue());
        }
      }
    }
  }
}
// [END secretmanager_delete_regional_secret_tag]
