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

// [START apikeys_undelete_api_key]
import com.google.api.apikeys.v2.ApiKeysClient;
import com.google.api.apikeys.v2.Key;
import com.google.api.apikeys.v2.UndeleteKeyRequest;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class UndeleteApiKey {

  public static void main(String[] args)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // TODO(developer): Replace these variables before running the sample.
    // Project ID or project number of the Google Cloud project.
    String projectId = "YOUR_PROJECT_ID";
    // The API key id to undelete.
    String keyId = "YOUR_KEY_ID";

    undeleteApiKey(projectId, keyId);
  }

  // Undeletes an API key.
  public static void undeleteApiKey(String projectId, String keyId)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (ApiKeysClient apiKeysClient = ApiKeysClient.create()) {

      // Initialize the undelete request and set the argument.
      UndeleteKeyRequest undeleteKeyRequest = UndeleteKeyRequest.newBuilder()
          .setName(String.format("projects/%s/locations/global/keys/%s", projectId, keyId))
          .build();

      // Make the request and wait for the operation to complete.
      Key undeletedKey = apiKeysClient.undeleteKeyAsync(undeleteKeyRequest)
          .get(3, TimeUnit.MINUTES);

      System.out.printf("Successfully undeleted the API key: %s", undeletedKey.getName());
    }
  }
}
// [END apikeys_undelete_api_key]