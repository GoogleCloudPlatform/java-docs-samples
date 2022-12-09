/*
 * Copyright 2022 Google Inc.
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

// [START auth_cloud_delete_api_key]

import com.google.api.apikeys.v2.ApiKeysClient;
import com.google.api.apikeys.v2.DeleteKeyRequest;
import com.google.api.apikeys.v2.Key;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class DeleteApiKey {

  public static void main(String[] args)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // TODO(Developer): Before running this sample,
    //  1. Replace the variable(s) below.
    //  2. Set up ADC as described in https://cloud.google.com/docs/authentication/external/set-up-adc
    //  3. Make sure you have the necessary permission to delete API keys.
    // Google Cloud project id that has the API key to delete.
    String projectId = "GOOGLE_CLOUD_PROJECT_ID";
    // The API key id to delete.
    String apiKeyId = "API_KEY_ID";

    deleteApiKey(projectId, apiKeyId);
  }

  // Deletes an API key.
  public static void deleteApiKey(String projectId, String apiKeyId)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the `apiKeysClient.close()` method on the client to safely
    // clean up any remaining background resources.
    try (ApiKeysClient apiKeysClient = ApiKeysClient.create()) {

      // Initialize the delete request and set the argument.
      DeleteKeyRequest deleteKeyRequest = DeleteKeyRequest.newBuilder()
          .setName(String.format("projects/%s/locations/global/keys/%s", projectId, apiKeyId))
          .build();

      // Make the request and wait for the operation to complete.
      Key deletedKey = apiKeysClient.deleteKeyAsync(deleteKeyRequest)
          .get(3, TimeUnit.MINUTES);

      System.out.printf("Successfully deleted the API key: %s", deletedKey.getName());
    }
  }
}
// [END auth_cloud_delete_api_key]