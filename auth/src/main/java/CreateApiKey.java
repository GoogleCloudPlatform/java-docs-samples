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

// [START auth_cloud_create_api_key]

import com.google.api.apikeys.v2.ApiKeysClient;
import com.google.api.apikeys.v2.ApiTarget;
import com.google.api.apikeys.v2.CreateKeyRequest;
import com.google.api.apikeys.v2.Key;
import com.google.api.apikeys.v2.LocationName;
import com.google.api.apikeys.v2.Restrictions;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CreateApiKey {

  public static void main(String[] args)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // TODO(Developer): Before running this sample,
    //  1. Replace the variable(s) below.
    //  2. Set up ADC as described in https://cloud.google.com/docs/authentication/external/set-up-adc
    //  3. Make sure you have the necessary permission to create API keys.
    String projectId = "GOOGLE_CLOUD_PROJECT_ID";

    createApiKey(projectId);
  }

  // Creates an API key.
  public static void createApiKey(String projectId)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the `apiKeysClient.close()` method on the client to safely
    // clean up any remaining background resources.
    try (ApiKeysClient apiKeysClient = ApiKeysClient.create()) {

      Key key = Key.newBuilder()
          .setDisplayName("My first API key")
          // Set the API key restriction.
          // You can also set browser/ server/ android/ ios based restrictions.
          // For more information on API key restriction, see:
          // https://cloud.google.com/docs/authentication/api-keys#api_key_restrictions
          .setRestrictions(Restrictions.newBuilder()
              // Restrict the API key usage by specifying the target service and methods.
              // The API key can only be used to authenticate the specified methods in the service.
              .addApiTargets(ApiTarget.newBuilder()
                  .setService("translate.googleapis.com")
                  .addMethods("translate.googleapis.com.TranslateText")
                  .build())
              .build())
          .build();

      // Initialize request and set arguments.
      CreateKeyRequest createKeyRequest = CreateKeyRequest.newBuilder()
          // API keys can only be global.
          .setParent(LocationName.of(projectId, "global").toString())
          .setKey(key)
          .build();

      // Make the request and wait for the operation to complete.
      Key result = apiKeysClient.createKeyAsync(createKeyRequest).get(3, TimeUnit.MINUTES);

      // For authenticating with the API key, use the value in "result.getKeyString()".
      // To restrict the usage of this API key, use the value in "result.getName()".
      System.out.printf("Successfully created an API key: %s", result.getName());
    }
  }
}
// [END auth_cloud_create_api_key]