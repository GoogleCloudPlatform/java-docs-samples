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

// [START secretmanager_bind_regional_secret_tag]
import com.google.cloud.resourcemanager.v3.CreateTagBindingRequest;
import com.google.cloud.resourcemanager.v3.TagBinding;
import com.google.cloud.resourcemanager.v3.TagBindingsClient;
import com.google.cloud.resourcemanager.v3.TagBindingsSettings;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class BindRegionalSecretTag {

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

    bindRegionalSecretTag(projectId, locationId, secretId, tagValueName);
  }

  // Bind a TagValue to a regional Secret by creating a TagBinding.
  public static TagBinding bindRegionalSecretTag(
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
      TagBinding tagBinding = TagBinding.newBuilder()
          .setTagValue(tagValueName)
          .setParent(parent)
          .build();

      CreateTagBindingRequest request = CreateTagBindingRequest.newBuilder()
          .setTagBinding(tagBinding)
          .build();

      TagBinding created = tagBindingsClient.createTagBindingAsync(request).get();
      System.out.printf("Created TagBinding: %s\n", created.getName());
      return created;
    }
  }
}
// [END secretmanager_bind_regional_secret_tag]
