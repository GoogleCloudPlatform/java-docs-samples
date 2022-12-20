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

// [START auth_cloud_lookup_api_key]

import com.google.api.apikeys.v2.ApiKeysClient;
import com.google.api.apikeys.v2.LookupKeyRequest;
import com.google.api.apikeys.v2.LookupKeyResponse;
import java.io.IOException;

public class LookupApiKey {

  public static void main(String[] args) throws IOException {
    // TODO(Developer): Before running this sample,
    //  1. Replace the variable(s) below.
    //  2. Set up ADC as described in https://cloud.google.com/docs/authentication/external/set-up-adc
    //  3. Make sure you have the necessary permission to view API keys.
    // API key string to retrieve the API key name.
    String apiKeyString = "API_KEY_STRING";

    lookupApiKey(apiKeyString);
  }

  // Retrieves name (full path) of an API key using the API key string.
  public static void lookupApiKey(String apiKeyString) throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the `apiKeysClient.close()` method on the client to safely
    // clean up any remaining background resources.
    try (ApiKeysClient apiKeysClient = ApiKeysClient.create()) {

      // Initialize the lookup request and set the API key string.
      LookupKeyRequest lookupKeyRequest = LookupKeyRequest.newBuilder()
          .setKeyString(apiKeyString)
          .build();

      // Make the request and obtain the response.
      LookupKeyResponse response = apiKeysClient.lookupKey(lookupKeyRequest);

      System.out.printf("Successfully retrieved the API key name: %s", response.getName());
    }
  }
}
// [END auth_cloud_lookup_api_key]