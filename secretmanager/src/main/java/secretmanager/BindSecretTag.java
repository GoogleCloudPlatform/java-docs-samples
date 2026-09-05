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

// [START secretmanager_bind_secret_tag]
import com.google.cloud.resourcemanager.v3.CreateTagBindingRequest;
import com.google.cloud.resourcemanager.v3.TagBinding;
import com.google.cloud.resourcemanager.v3.TagBindingsClient;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class BindSecretTag {

  public static void main(String[] args) throws Exception {
    // TODO(developer): replace these variables before running the sample.
    
    // This is the id of the GCP project
    String projectId = "your-project-id";
    // This is the id of the secret to act on
    String secretId = "your-secret-id";
    // Tag value to bind, e.g. "tagValues/123"
    String tagValueName = "your-tag-value";

    bindSecretTag(projectId, secretId, tagValueName);
  }

  // Bind a TagValue to a Secret by creating a TagBinding.
  public static TagBinding bindSecretTag(String projectId, String secretId, String tagValueName)
      throws IOException, InterruptedException, ExecutionException {

    String parent = String.format("//secretmanager.googleapis.com/projects/%s/secrets/%s",
        projectId, secretId);

    try (TagBindingsClient tagBindingsClient = TagBindingsClient.create()) {
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
// [END secretmanager_bind_secret_tag]
